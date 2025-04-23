class Solution {
    public int fib(int n) {
        // 请完成代码
        if(n == 0) {
            return 0;
        }
        if(n<=2) {
            return 1;
        }
        return fib(n-1)+fib(n-2);
    }

public static void main(String[] args) {
    Solution solution = new Solution();
    Integer n = Integer.parseInt(args[0]);
    int result = solution.fib(n);
    System.out.print(result);
}
}