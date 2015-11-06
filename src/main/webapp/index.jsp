<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.pwr.main.Report,java.util.Date" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="css/jquery-ui.css">
<link rel="stylesheet" href="css/jquery.qtip.min.css">
<link href="css/main.css" rel="stylesheet">
<title>SKiNP</title>
</head>
<body>
<div id="background"></div>
<div id="wrapper">
	<div id="user"<%=((session.getAttribute("userID")!=null)?" class=\"signedIn\"":"") %>>
		<div class="signIn<%=((session.getAttribute("userID")!=null)?" inv":"") %>">
			<input type="email" class="email" placeholder="e-mail"><br>
			<input type="password" class="password" placeholder="hasło"><br>
			<button>Zaloguj</button>
		</div>
		<div class="data <%=((session.getAttribute("userID")!=null)?"":" inv") %>">
			<span class="name">Ładowanie...</span>
			<span class="email">Ładowanie...</span>
			<button class="signOut inv">Wyloguj</button>
		</div>
		<div class="zones inv">
			<ul></ul>
		</div>
	</div>
	<ul id="menu" class="inv">
		<li data-slug="lastEvents" class="active">Ostatnie zdarzenia</li>
		<li data-slug="reports">Raporty</li>
		<!--<li data-slug="admin-users">Zarządzanie użytkownikami</li>-->
		<li data-slug="admin-zones">Zarządzanie strefami</li>
	</ul>
	<div id="container" class="inv">
		<div id="lastEvents">
			<table class="events inv">
				<thead>
					<tr>
						<th>Data</th>
						<th class="zoneName">Strefa</th>
						<th>Zdarzenie</th>
					</tr>
				</thead>
				<tbody></tbody>
			</table>
		</div>
		<div id="zones">
			<ul></ul>
			<button class="add">Dodaj nową strefę</button>
		</div>
		<div id="reports">
			<div id="reportsUsers">
			<h1>Czas pracy</h1>
			<div class="options"><label>Użytkownik:</label><select class="user" disabled><option>Ładowanie...</option></select><br>
			<label>Zakres dat:</label><input class="from" value="<%=Report.toSQLDate(new Date())%>">–<input class="to" value="<%=Report.toSQLDate(new Date())%>"></div>
				<table class="events inv">
					<thead>
					<tr>
						<th>Data</th>
						<th class="zoneName">Strefa</th>
						<th>Zdarzenie</th>
					</tr>
					</thead>
					<tbody></tbody>
				</table>
				<div class="time inv">Całkowity czas pracy w wybranym zakresie: <span class="totalTime">...</span></div>
			</div><!--
			--><div id="reportsZones">
				<h1>Raport pracowników w strefach</h1>
				<div class="options"><label>Data:</label><input class="date" placeholder="YYYY-MM-DD"><br><label>Godzina:</label><input class="time" placeholder="HH:MM:SS"></div>
				<ul></ul>
			</div>
		</div>
	</div>
</div>
<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.min.js"></script>
<script src="js/jquery.qtip.min.js"></script>
<script src="js/main.js"></script>
</body>
</html>