<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3">
<head>
    <title>Hello World!</title>
</head>
<body>
<h1>params: ${param}</h1>

<h1>header: ${header}</h1>

<h1>cookies: ${cookies}</h1>

<h1>耗时: ${time}</h1>

<h1>qps: ${qps}</h1>

<h1>内存使用率: ${memoryResult}</h1>

<h1>CPU使用情况: ${threadResult}</h1>

<h1>磁盘使用率: ${driveResult}</h1>

</body>

</html>