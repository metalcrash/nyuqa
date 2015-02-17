<%@ page language="java" import="java.util.*,nyuqa.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<script src="jquery-2.1.1.min.js"></script>
<script src="highlightRegex.min.js"></script>
<title>Question Answer System-NLP</title>
<style>
	body {background-color: lightgrey}
	h1   {text-align: center; font-family:courier}
	form {text-align: center; font-family:verdana}
	p {text-align: left; font-family:courier;font-size:100%;}
	#notice {text-align: center; font-family:verdana;display:none}
	#time {text-align: center; font-family:verdana}
	hr {display:none}
</style>
</head>
<% 
String answer1;
String answer2;
String answer3;
String query="a";
//String[] keywords;
int type=(int)request.getAttribute("type");
long time=0;
if(type==0){answer1="";answer2="";answer3="";}
else{
//keywords=(String [])request.getAttribute("keywords");
time=(long)request.getAttribute("time");
query=(String)request.getAttribute("query");
String[] answers=(String [])request.getAttribute("answers");
answer1=answers[2];
answer2=answers[1];
answer3=answers[0];
}
%>
<script type="text/javascript">
$(document).ready(function(){
	$("#notice").hide();
if(<%=type %>==0){
}
if(<%=type %>==1){
	console.log("aha");
	$("#q").val("<%=query %>");
	$("#notice").show();
	$("#time").text("responsed in <%=time %>ms");
	$("#time").show();
	$("hr").show();
}
});
</script>
<body>

<h1><strong>NATURAL LANGUAGE PROCESSING-TERM PROJECT<br>QUESTION-ANSWER SYSTEM</strong></h1>

<form action="nyuqa" method="GET">
Please Input Your Question: <input id="q" type="text" name="qstn" size="75" value="">
<input id="submit" type="submit" value="Submit"><br><br>
<p id="notice">Thanks for the input, the answer to your question is:</p><br>
</form>
<hr>
<p><%=answer1%></p>
<hr>
<p><%=answer2%></p>
<hr>
<p><%=answer3%></p>
<hr>
<p id="time"></p>
</body>
</html>
