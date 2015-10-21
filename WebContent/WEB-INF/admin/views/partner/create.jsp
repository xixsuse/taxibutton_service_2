<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


	<form method="post">
		<br/>name <input type="text" name="name" value="Желтое такси" />
		<br/>clid <input type="text" name="apiId" value="1"/>
		<br/>apikey <input type="text" name="apiKey" value="1"/>
		<br/>url <input type="text" name="apiurl" value="http://" />
		<br/>tariff:
		<input type="radio" name="tarifftype" value="0" checked />xml
		<input type="radio" name="tarifftype" value="1" />json
		<br/>tariff url <input type="text" name="tariffurl" value="http://"/>
		<br/>driver url <input type="text" name="driverurl" value="http://"/>
		<br/>maparea url <input type="text" name="mapareaurl" value="http://"/>
		<br/>cost url <input type="text" name="costurl" value="http://"/>
		<br/>TimeZone ID <input type="text" name="timezoneId" value="UTC"/>
		
		<br />
		<br /> Map areas:
		<br />
		<c:forEach items="${mapareas}" var="maparea">
			<input type="checkbox" name="mapareas" value="${maparea.getId()}" />${maparea.getName()}<br />
		</c:forEach>
		
		<br /> <input type="submit" value="save" />
	</form>