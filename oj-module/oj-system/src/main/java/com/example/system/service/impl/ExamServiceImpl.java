package com.example.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.security.exception.ServiceException;
import com.example.core.enums.ResultCode;
import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionDTO;
import com.example.system.domain.exam.entity.Exam;
import com.example.system.domain.exam.entity.ExamQuestion;
import com.example.system.domain.exam.vo.ExamQueryVO;
import com.example.system.domain.question.entity.Question;
import com.example.system.mapper.ExamMapper;
import com.example.system.mapper.ExamQuestionMapper;
import com.example.system.mapper.QuestionMapper;
import com.example.system.service.ExamService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamQuestionMapper,ExamQuestion> implements ExamService{

    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<ExamQueryVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.getExamList(examQueryDTO);
    }

    @Override
    public int add(ExamAddDTO examAddDTO) {
        //检验参数合法性 标题是否重复 开始时间 结束时间的合法性判断
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getTitle, examAddDTO.getTitle()));
        if(!CollectionUtils.isEmpty(exams)){
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        if(LocalDateTime.now().isAfter(examAddDTO.getStartTime())) {
            throw new ServiceException(ResultCode.FAILED_EXAM_TIME);
        }
        if(examAddDTO.getEndTime().isBefore(examAddDTO.getStartTime())) {
            throw new ServiceException(ResultCode.FAILED_EXAM_TIME);
        }
        Exam exam = new Exam();
        BeanUtil.copyProperties(examAddDTO, exam);
        return examMapper.insert(exam);
    }

    @Override
    @Transactional
    public boolean addQuestion(ExamQuestionDTO dto) {
        //竞赛是否存在
        examExists(dto);
        //问题是否都存在 只有都存在才合法
        //如果传过来就是空的 直接返回 不再执行下面的逻辑
        if(CollectionUtils.isEmpty(dto.getQuestionIds())) {
            return true;
        }
        List<Question> questions = questionMapper.selectBatchIds(dto.getQuestionIds());
        //比较大小 如果查出来的记录数少了 就说明存在问题
        if(CollectionUtils.isEmpty(questions) || questions.size() < dto.getQuestionIds().size()) {
            throw new ServiceException(ResultCode.FAILED_QUESTION_NOT_EXISTS);
        }
        //检验通过 存入数据库 要先转化为ExamQuestion实体对象
        return saveQuestion(dto);
    }

    @Transactional
    public boolean saveQuestion(ExamQuestionDTO dto) {
        List<ExamQuestion> examQuestions = new ArrayList<>();
        //遍历问题集合 拼装集合插入
        int order = 0;
        for (Long questionId : dto.getQuestionIds()) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setQuestionId(questionId);
            examQuestion.setExamId(dto.getExamId());
            examQuestion.setQuestionOrder(order++);
            examQuestions.add(examQuestion);
        }
        return saveBatch(examQuestions);
    }

    private void examExists(ExamQuestionDTO dto) {
        Exam exam = examMapper.selectById(dto.getExamId());
        if(exam == null){
            throw new ServiceException(ResultCode.FAILED_EXAM_NOT_EXISTS);
        }
    }
}
