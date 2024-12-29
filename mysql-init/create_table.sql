-- 创建库
create database if not exists zoj;

-- 切换库
use zoj;

-- auto-generated definition
create table if not exists user
(
    id           bigint auto_increment comment 'id'
    primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
    )
    comment '用户' collate = utf8mb4_unicode_ci;

create index idx_unionId
    on user (unionId);

-- auto-generated definition
create table question
(
    id          bigint auto_increment comment 'id'
        primary key,
    title       varchar(512)                       null comment '标题',
    content     text                               null comment '内容',
    answer      text                               null comment '题解',
    tags        varchar(1024)                      null comment '题目标签（json 数组）',
    submitNum   int      default 0                 null comment '提交数量',
    acceptNum   int      default 0                 null comment '通过数量',
    judgeConfig text                               null comment '判题的配置（json对象）',
    judgeCase   text                               null comment '判题用例（json数组）',
    userId      bigint                             not null comment '创建用户 id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '题目' collate = utf8mb4_unicode_ci;

create index idx_question_userId
    on question (userId);



-- auto-generated definition
create table question_submit
(
    id         bigint auto_increment comment 'id'
        primary key,
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '提交用户 id',
    language   varchar(128)                       not null comment '语言',
    code       text                               not null comment '用户代码',
    status     int      default 0                 not null comment '判题状态（0-等待中 1-判题中 2-成功 3-失败)',
    judgeInfo  text                               null comment '判题信息（json 对象）',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '题目提交';

create index idx_questionId
    on question_submit (questionId);

create index idx_submit_userId
    on question_submit (userId);

INSERT INTO user (

    userAccount,
    userPassword,
    unionId,
    mpOpenId,
    userName,
    userAvatar,
    userProfile,
    userRole,
    isDelete
) VALUES
    ( 'userAccount', '54621f5454e24864f640e7b8113617fe', NULL, NULL, 'zsg', NULL, NULL, 'admin',  0);

INSERT INTO question (
    title, content, answer, tags, submitNum, acceptNum, judgeConfig, judgeCase, userId, createTime, updateTime, isDelete
) VALUES (
             'Max Sum',
             '## Problem Description\nGiven a sequence a[1],a[2],a[3]......a[n], your job is to calculate the max sum of a sub-sequence. For example, given (6,-1,5,4,-7), the max sum in this sequence is 6 + (-1) + 5 + 4 = 14.\n## Input\nThe first line of the input contains an integer T(1<=T<=20) which means the number of test cases. Then T lines follow, each line starts with a number N(1<=N<=100000), then N integers followed(all the integers are between -1000 and 1000).\n## Output\nFor each test case, you should output two lines. The first line is "Case #:", # means the number of the test case. The second line contains three integers, the Max Sum in the sequence, the start position of the sub-sequence, the end position of the sub-sequence. If there are more than one result, output the first one. Output a blank line between two cases.\n## Sample Input\n2\n\n5 6 -1 5 4 -7\n\n7 0 6 -1 1 -6 7 -5\n## Sample Output\nCase 1:\n14 1 4\n\nCase 2:\n7 1 6',
             'import java.util.Scanner;\nimport java.util.Arrays;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner scanner = new Scanner(System.in);\n\n        int t = scanner.nextInt();  // 读取测试用例数量\n\n        for (int testCase = 1; testCase <= t; testCase++) {\n            int n = scanner.nextInt();  // 读取数组的大小\n            int[] a = new int[n + 1];   // 数组下标从1开始\n\n            // 读取数组元素\n            for (int i = 1; i <= n; i++) {\n                a[i] = scanner.nextInt();\n            }\n\n            // 动态规划求解最大子段和问题\n            int maxSum = a[1];  // 初始最大和为第一个元素\n            int currentStart = 1, left = 1, right = 1;\n            int tempStart = 1;  // 临时起始下标\n\n            for (int i = 2; i <= n; i++) {\n                if (a[i - 1] < 0) {\n                    tempStart = i;  // 如果前面的和为负数，重置起始下标\n                } else {\n                    a[i] += a[i - 1];  // 累加前面的和\n                }\n\n                // 更新最大和及左右边界\n                if (a[i] > maxSum) {\n                    maxSum = a[i];\n                    left = tempStart;\n                    right = i;\n                }\n            }\n\n            // 输出结果\n            System.out.println("Case " + testCase + ":");\n            System.out.println(maxSum + " " + left + " " + right);\n            if (testCase < t) {\n                System.out.println();  // 多个测试用例之间空行\n            }\n        }\n\n        scanner.close();\n    }\n}',
             '["simple","dp"]',
             0, 0,
             '{"timeLimit":1000,"memoryLimit":32768,"stackLimit":1000}',
             '[{"input":"2\\n5 6 -1 5 4 -7\\n7 0 6 -1 1 -6 7 -5","output":"Case 1:\\n14 1 4\\n\\nCase 2:\\n7 1 6"}]',
             1,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP,
             0
         );
INSERT INTO question (title, content, answer, tags, judgeConfig, judgeCase, userId)
VALUES
    (
        "a + b",
        "## Description\n\nCalculate a+b\n## Input\n\nTwo integer a,b (0<=a,b<=10)\n## Output\n\nOutput a+b\n## Sample Input\n\n1 2\n## Sample Output\n\n3",
        "import java.io.*;\nimport java.util.*;\npublic class Main\n{\n            public static void main(String args[]) throws Exception\n            {\n                    Scanner cin=new Scanner(System.in);\n                    int a=cin.nextInt(),b=cin.nextInt();\n                    System.out.println(a+b);\n            }\n}",
        '["simple","stack"]',
        '{"timeLimit":1000,"memoryLimit":32768,"stackLimit":1000}',
        '[{"input":"1 2","output":"3"},{"input":"3 4","output":"7"}]',
        1
    );
INSERT INTO question (title, content, answer, tags, judgeConfig, judgeCase, userId)
VALUES
    (
        "Zhao Shen Niu\'s Game",
        "# Zhao Shen Niu\'s Game\n\n## Problem Description\n\nIn DNF, Zhao Shen Niu has a creator, who has k mana points and m skills. Each skill consumes mana a_i and deals damage b_i. The boss\'s health points are n. Your task is to find out which skill can kill the boss.\n\nOf course, Zhao Shen Niu\'s technique is quite poor, so he can only use one skill per round, but each skill can be used multiple times.\n\n## Input Format\n\nThe first line contains three integers, k, m, and n.\n\nThe next m lines, each line contains two integers: the i-th line contains mana cost a_i and damage b_i.\n\n## Output Format\n\nOutput a single line with the skill index that can kill the boss. If there are multiple, output them in ascending order, separated by a space. If no skill can kill the boss, output \`-1\`.\n\n## Sample #1\n\n### Sample Input #1\n\n\`\`\`\n100 3 5000\n20 1000\n90 1\n110 10000\n\`\`\`\n\n### Sample Output #1\n\n\`\`\`\n1\n\`\`\`",
        "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int k = sc.nextInt(), m = sc.nextInt(), n = sc.nextInt();\n        int[][] skills = new int[m][2];\n        for (int i = 0; i < m; i++) {\n            skills[i][0] = sc.nextInt();\n            skills[i][1] = sc.nextInt();\n        }\n        boolean found = false;\n        for (int i = 0; i < m; i++) {\n            if (skills[i][0] <= k && skills[i][1] * (k / skills[i][0]) >= n) {\n                System.out.print((i + 1) + \" \");\n                found = true;\n            }\n        }\n        if (!found) {\n            System.out.println(\"-1\");\n        }\n    }\n}",
        '["simple","beginer"]',
        '{"timeLimit":1000,"memoryLimit":32768,"stackLimit":1000}',
        '[{"input":"100 3 5000\\n20 1000\\n90 1\\n110 10000","output":"1"},{"input":"50 4 10\\n60 100\\n70 1000\\n80 1000\\n90 0","output":"-1"}]',
        1
    );
