<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Password Recovery</title>
    <style>

        * {
            box-sizing: border-box;
        }

        body {
            background-color: #fafafa;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .c-email {
            border-radius: 40px;
            overflow: hidden;
            box-shadow: 0px 7px 22px 0px rgba(0, 0, 0, 0.1);
        }

        .c-email__header {
            background-color: #0fd59f;
            width: 100%;
            height: 60px;
        }

        .c-email__header__title {
            font-size: 23px;
            font-family: "Open Sans";
            height: 60px;
            line-height: 60px;
            margin: 0;
            text-align: center;
            color: white;
        }

        .c-email__content {
            width: 100%;
            height: 300px;
            background-color: #fff;
            padding: 15px;
        }

        .c-email__content__text {
            font-size: 20px;
            text-align: center;
            color: #343434;
            margin-top: 0;
        }

        .c-email__code {
            display: block;
            width: 60%;
            margin: 30px auto;
            background-color: #ddd;
            border-radius: 40px;
            padding: 20px;
            text-align: center;
            font-size: 36px;
            font-family: "Open Sans";
            letter-spacing: 10px;
            box-shadow: 0px 7px 22px 0px rgba(0, 0, 0, 0.1);
        }

        .c-email__footer {
            width: 100%;
            height: 60px;
            background-color: #fff;
        }

        .text-title {
            font-family: "Open Sans";
        }

        .text-center {
            text-align: center;
        }

        .text-italic {
            font-style: italic;
        }

        .opacity-30 {
            opacity: 0.3;
        }

        .mb-0 {
            margin-bottom: 0;
        }
    </style>
</head>
<body>
<div class="c-email">
    <div class="c-email__header">
        <h1 class="c-email__header__title">Your Password Recovery Code</h1>
    </div>
    <div class="c-email__content">
        <p class="c-email__content__text text-title">
            Enter this password code to change your password:
        </p>
        <div class="c-email__code">
            <span class="c-email__code__text">${recoveryCode}</span>
        </div>
        <p class="c-email__content__text text-italic opacity-30 text-title mb-0">
            Password recovery code is valid only for 5 minutes
        </p>
    </div>
    <div class="c-email__footer"></div>
</div>
</body>
</html>