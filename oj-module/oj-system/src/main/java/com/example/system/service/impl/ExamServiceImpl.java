package com.example.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.security.exception.ServiceException;
import com.example.core.constants.Constants;
import com.example.core.enums.ResultCode;
import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamEditDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionDTO;
import com.example.system.domain.exam.entity.Exam;
import com.example.system.domain.exam.entity.ExamQuestion;
import com.example.system.domain.exam.vo.ExamAddVO;
import com.example.system.domain.exam.vo.ExamQueryVO;
import com.example.system.domain.exam.vo.ExamVO;
import com.example.system.domain.question.entity.Question;
import com.example.system.domain.question.vo.QuestionQueryVO;
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
    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Override
    public List<ExamQueryVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.getExamList(examQueryDTO);
    }

    @Override
    public ExamAddVO add(ExamAddDTO examAddDTO) {
        checkDTO(examAddDTO,null);
        Exam exam = new Exam();
        BeanUtil.copyProperties(examAddDTO, exam);
        if(examMapper.insert(exam)<=0) {
            throw new ServiceException(ResultCode.FAILED);
        }
        return new ExamAddVO(exam.getExamId());
    }

    private void checkDTO(ExamAddDTO examAddDTO,Long examId) {
        //检验参数合法性 标题是否重复 开始时间 结束时间的合法性判断
        //编辑时判断 要传入ne 也就是标题相同时不能是相同的ID
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getTitle, examAddDTO.getTitle())
                .ne(examId!=null,Exam::getExamId,examId)
        );
        if(!CollectionUtils.isEmpty(exams)){
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        if(LocalDateTime.now().isAfter(examAddDTO.getStartTime())) {
            throw new ServiceException(ResultCode.FAILED_EXAM_TIME);
        }
        if(examAddDTO.getEndTime().isBefore(examAddDTO.getStartTime())) {
            throw new ServiceException(ResultCode.FAILED_EXAM_TIME);
        }
    }

    @Transactional
    @Override
    public boolean addQuestion(ExamQuestionDTO dto) {
        //竞赛是否存在
        getExamById(dto.getExamId());
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

    @Override
    public ExamVO detail(Long examId) {
        ExamVO examVO = new ExamVO();
        //获取竞赛信息
        Exam exam = getExamById(examId);
        //将获取到的竞赛信息放到vo当中
        BeanUtil.copyProperties(exam, examVO);
        //查到了竞赛信息存在之后 获取题目信息
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getQuestionId)
                .eq(ExamQuestion::getExamId, examId)
        );
        //如果题目为空就直接返回 不需要进行后续的操作
        if(CollectionUtils.isEmpty(examQuestions)) {
            return examVO;
        }
        //提取题目ID列表
        List<Long> questionIds = examQuestions.stream().map(ExamQuestion::getQuestionId).toList();
        //根据题目ID列表批处理查询 返回题目的信息
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        //将题目信息列表转换为题目VO列表
        List<QuestionQueryVO> questionVOS = BeanUtil.copyToList(questions, QuestionQueryVO.class);
        //转换好之后 设置到examVO当中
        examVO.setExamQuestionList(questionVOS);
        return examVO;
    }

    @Override
    public int edit(ExamEditDTO examEditDTO) {
        //判断是否有这个竞赛
        Exam exam = getExamById(examEditDTO.getExamId());
        //判断参数是否有问题
        checkDTO(examEditDTO,examEditDTO.getExamId());
        //对竞赛信息编辑
        exam.setStartTime(examEditDTO.getStartTime());
        exam.setEndTime(examEditDTO.getEndTime());
        exam.setTitle(examEditDTO.getTitle());
        return examMapper.updateById(exam);
    }

    @Override
    public int deleteQuestion(Long examId, Long questionId) {
        //还是一样先要校验exam 获取exam 然后再对其是否开赛做出判断
        Exam exam = getExamById(examId);
        //开赛了之后就无法修改了
        if(LocalDateTime.now().isAfter(exam.getStartTime())) {
            throw new ServiceException(ResultCode.FAILED_START_TIME_PASSED);
        }
        //进行删除操作
        return examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId)
                .eq(ExamQuestion::getQuestionId, questionId)
        );
    }

    @Override
    public int delete(Long examId) {
        //校验是否存在竞赛
        Exam exam = getExamById(examId);
        if(LocalDateTime.now().isAfter(exam.getStartTime())) {
            throw new ServiceException(ResultCode.FAILED_START_TIME_PASSED);
        }
        examQuestionMapper.delete(
                new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamId,examId)
        );
        return examMapper.deleteById(examId);
    }

    @Override
    public int cancelPublish(Long examId) {
        //也是先校验是否存在竞赛
        Exam exam = getExamById(examId);
        //校验是否已经开始 已开始的竞赛无法撤销
        if(LocalDateTime.now().isAfter(exam.getStartTime())) {
            throw new ServiceException(ResultCode.FAILED_START_TIME_PASSED);
        }
        //校验成功 修改字段
        exam.setStatus(Constants.NOT_PUBLISHED);
        return examMapper.updateById(exam);
    }

    @Override
    public int publish(Long examId) {
        //检验竞赛是否存在
        Exam exam = getExamById(examId);
        //时间检验
        if(LocalDateTime.now().isAfter(exam.getStartTime())) {
            throw new ServiceException(ResultCode.FAILED_START_TIME_PASSED);
        }
        //检验当前竞赛是否包含题目 若没有题目不可以进行发布
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamId, examId)
        );
        if(CollectionUtils.isEmpty(examQuestions)) {
            throw new ServiceException(ResultCode.FAILED_EXAM_HAS_NO_QUESTION);
        }
        //修改竞赛信息 状态字段
        exam.setStatus(Constants.IS_PUBLISHED);
        return examMapper.updateById(exam);
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

    private Exam getExamById(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if(exam == null){
            throw new ServiceException(ResultCode.FAILED_EXAM_NOT_EXISTS);
        }
        return exam;
    }
}
