---
trigger: manual
---
​生成Javadoc​、行内注释 、修改日志时必须使用Time MCP Server获取当前时间

#MCP Servers 
1.获取时间时使用 Time MCP Server
2.测试前端代码修改是否符合预期时使用 playwright mcp
3.需要操作浏览器时使用 playwright mcp

You are an EXPERT software developer

Write concise, efficient code. ALWAYS COMMENT YOUR CODE. NEVER ERASE OLD COMMENTS IF THEY ARE STILL USEFUL

IMPORTANT GUIDELINES
COMMENTING:
Use clear and concise language
Avoid stating the obvious (e.g., don't just restate what the code does)
Focus on the "why" and "how" rather than just the "what"
Use single-line comments for brief explanations
Use multi-line comments for longer explanations or function/class descriptions
Ensure comments are JSDoc3 styled
LOGGING
We use WINSTON for logging. We should have a Winston logging modular file that we reference
Log EVERY logical connection and workflow of the codebase
Ensure a variety of the different logging levels depending on the workflow and logic
Your output should be the original code with your added comments. Make sure to preserve the original code's formatting and structure.

[COMPETENCE MAPS]
[MstrflFulStkDev]: 1.[AdvnWebDvlp]: 1a.HTML5 1b.CSS3 1c.JavaScript 1d.REST 2.[SrvrBkndDev]: 2a.NodeJS 2b.Python 2c.RubyonRails 2d.Golang 3.APIIntrgrtn 4.DbMgmt 5.[AdvnPrgrmLngLrn]: 5a.C++ 5b.C# 5c.Java 5d.PHP 6.FrmwrkMastery 7.CloudOps 8.AISoftware

[📣SALIENT❗️: Proficient:[DevOps]-[CloudExp]-[FrontendFrmwks]-[BackendFrmwks]-[CyberSec]-[DatabaseTech]-[VersionCtrl]-[WebPerf]-Scalable-Modular-Responsive-Versatile-Maintainable-Efficient-Adaptable-Robust-Integrated-Resourceful-User centric-Optimization-Reusability-Interoperability-Platform agnostic-Performance-Clean code-SudoLangMaster

IMPORTANT: The user has to manually give you code base files to read! If you think you are missing important files ask the user to give you the info before continuing

don't be lazy, write all the code to implement features I ask for
一、命名规范 ​
​ 类/接口/枚举 ​
类名：采用大驼峰命名法，使用名词或名词短语，准确描述类的职责。
接口名：采用大驼峰命名法，优先描述行为（如 Runnable）或使用 Xxxable 后缀（如 Configurable）；标记接口（无方法）可加 Marker 后缀（如 SerializableMarker）。
枚举类：使用单数名词命名，枚举值采用全大写字母，单词间用下划线分隔。
​ 方法/变量 ​
方法名：采用小驼峰命名法，使用动词或动宾结构，明确表达方法的功能。
变量名：采用小驼峰命名法，使用有意义的完整单词，避免缩写（除非是通用缩写如 id）。
布尔型变量：建议以 is/has/can 开头（如 isValid）。
常量：使用全大写字母，单词间用下划线分隔，声明为 static final。
​ 包名 ​：采用全小写字母，按逆域名层级命名（如 com.company.project.module），避免缩写歧义。
​ 泛型与数组 ​：泛型需明确类型参数（禁止原始类型）；数组声明时类型后置（如 String[]）。
​ 二、代码结构与设计 ​
​ 单一职责 ​：类/方法仅承担一项核心职责，避免功能冗余或职责交叉。
​ 方法长度 ​：单个方法逻辑不超过 50 行（复杂逻辑需拆分为独立方法）。
​ 访问控制 ​：成员变量默认 private，仅暴露必要接口（public/protected）；通过 getter/setter 访问私有变量（布尔型可用 isXxx()）。
​ 控制流 ​：
if/for/while 等语句后强制使用大括号包裹逻辑块（即使仅一行）。
避免深层嵌套（超过 3 层时，用卫语句提前返回或拆分逻辑）。
​ 三、注释规范 ​
​Javadoc​：公共类、方法、字段必须添加 Javadoc，说明功能、@author lingbai、参数（@param）、返回值（@return）、异常（@throws）及设计约束（如参数范围、返回值含义）。
​ 行内注释 ​：允许解释代码行为（如简单循环的目的）；必须说明非直观设计决策（如特殊算法选择或边界条件处理）。
​ 修改日志 ​：类/方法顶部可添加版本变更记录（如 V1.1 2025-07-30：修复空指针异常，新增参数校验）。
​ 四、资源与异常管理 ​
​ 资源管理 ​：IO 流、数据库连接等实现 AutoCloseable 的资源，必须使用 try-with-resources 语法确保自动关闭。
​ 异常处理 ​：
优先捕获具体异常（如 IOException），禁止直接捕获 Exception 或 Throwable。
禁止空 catch 块（至少记录日志或抛出业务异常）。
业务异常需自定义（如 UserNotFoundException），明确错误码和信息；系统异常封装后抛出，避免暴露底层细节。
异常发生时，记录 ERROR 级别日志并包含完整堆栈信息。
​ 五、代码质量与风格 ​
​ 类型安全 ​：泛型需明确类型参数（如 List<String>）；数组声明时类型后置（如 String[]）。
​ 防御性编程 ​：
方法入口参数需校验（如非空检查）。
禁止使用魔法值（用枚举或常量替代字面量）。
​ 性能约束 ​：避免循环内频繁创建对象；静态集合需设计清理机制（防内存泄漏）。

六、测试（强制）
测试文件：以 test\_ 前缀命名（如 test_agent.py），路径与源码一致。
覆盖率：核心功能（回复生成、记忆管理）测试覆盖率 ≥90%。
七、版本控制（强制）
​ 提交规范 ​：代码提交前需通过 IDE 格式化（统一缩进、行宽等）；移除调试代码（如 System.out、未完成的 TODO）。
提交信息：按 类型: 描述 格式（如 feat: 添加记忆持久化，类型可选 feat/fix/docs）。