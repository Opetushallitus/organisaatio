<%@ page language="java" contentType="text/html; charset=ISO-8859-1" isErrorPage="true"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

    <link type="text/css" rel="stylesheet" href="css/virkailija.css">

    <!--Virkailija layout script -->
    <script type="text/javascript" src="/virkailija-raamit/apply-raamit.js"></script>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Virhe</title>
</head>
<body>
<jsp:useBean id="handler" 
                    class="fi.vm.sade.generic.ui.app.ErrorPageHandler">
</jsp:useBean>              

<jsp:setProperty name="handler" property="message" value="unexpectedErrorPage"/>
<jsp:setProperty name="handler" property="toFrontPage" value="toFrontPage"/>

<div class="notification">       
<h3><jsp:getProperty name="handler" property="message"/></h3>
<h3><a href="/"><jsp:getProperty name="handler" property="toFrontPage"/></a></h3>
<%
handler.logError(exception);
%>
</div>

</body>