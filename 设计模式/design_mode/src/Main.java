//思路：动态规划，打表法
// 创建数组dp，   dp[i][j] = n   这里i表示步数（从1到n），j表示最终红色格子数 (绝对值) ，n表示总共可能情况数
// 动态方程   dp[i][j] = dp[i-1][j-1]  或者   dp[i][j] = dp[i-1][j-1]
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        //创建dp数组,一维代表步数，二维代表最终红色格子数，值代表可能情况数目
        int[][] dp = new int [n][n+1];
        //初始化，第一步一定染红一个
        dp[0][1] = 1;
        for(int i = 1; i < n; i++) {//每一步

            for(int j = 1; j < n; j++) {//这里遍历上一步的
                if(dp[i-1][j-1] > 0) {
                    dp[i][j] += 1;//离开原点，有两种情况：  路程绝对值+1，  路程绝对值不变；往回走，路程绝对值不变

                    dp[i][j-1] += 2;//往回走，路程绝对值不变
                }
            }
        }
        //计算期望值
        double ex = 0;
        for(int i = 1; i <= n; i++) {
            ex += dp[n-1][i] * i;
        }
        ex /= 4;
        System.out.println(ex);
        //System.out.print(3.4);
    }
}
