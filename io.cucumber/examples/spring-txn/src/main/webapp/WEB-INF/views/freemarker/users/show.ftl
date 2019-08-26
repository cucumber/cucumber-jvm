<!DOCTYPE html>
<html>
  <head>
    <title>${user.username}'s page</title>
  </head>
  <body>
    ${user.username}'s messages
    <#list user.messages as message>
       <p>${message.content}</p>
    </#list>
  </body>
</html>
