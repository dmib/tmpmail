<html>
<head>
    <title>
        10 minuts mail
    </title>
</head>
<body>
<style>
    html {
        font-family: Arial;
        color: #333333;
    }

    body {
        background: aliceblue;
        margin: 0;
    }

    #container {
        width: 100%;
        margin: 0 auto;
        background: azure;
    }

    #header {
        width: 100%;
        height: 60px;
        border-bottom: 1px solid #333333;
        background: lightskyblue
    }

    #logo {
        float: left;
        width: 40px;
        height: 40px;
        margin: 10px;
        background: aliceblue
    }

    #top_info {
        float: right;
        width: 100%;
        height: 40px;
        background: aliceblue
    }

    #mail_address {
        background: slategray;
        height: 10%;
    }

    #banner {
        background: aliceblue;
        height: 50%;
    }

    #mails {
        width: 100%;
    }

    td {
        text-align: center
    }

    #footer {
        background: lightskyblue;
        height: 150px
    }
</style>
<div id="container">
    <div id="header">
        <div id="logo">LOGO</div>
        <div id="top_info"></div>
    </div>
    <div id="content_area">
        <div id="mail_address">
            ${tempMail.email}
            <button onclick="window.location.href='/mails'" type="button">Refresh</button>
        </div>
        <div id="banner">
            <table id="mails">
                <tr>
                    <th>Sender</th>
                    <th>Subject</th>
                    <th>Date</th>
                </tr>
                <#list mails as mail>
                    <tr>
                        <td>${mail.sender}</td>
                        <td>${mail.subject}</td>
                        <td>${mail.date}</td>
                    </tr>
                </#list>
            </table>
        </div>
    </div>
    <div id="footer">

    </div>
</div>
</body>
</html>