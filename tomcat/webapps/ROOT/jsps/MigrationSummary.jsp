<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
	<head>
		<title>Generic Migration Tool</title>
		<link href="/css/styles.css?t=<s:property value="time"/>" type="text/css" rel="stylesheet" />
		<link href="/css/jquery-ui-1.10.3.custom.min.css" media="screen" rel="stylesheet" type="text/css"></link>
		<link href="/css/jquery.shadow.css" media="screen" rel="stylesheet" type="text/css"></link>		
		<script type="text/javascript" src="/javascript/jquery-1.9.0.js"></script>
		<script type="text/javascript" src="/javascript/jquery-ui-1.10.3.custom.min.js"></script>
		<script type="text/javascript" src="/javascript/json2.js"></script>
		<script type="text/javascript" src="/javascript/jquery.shadow.js"></script>	
		<script type="text/javascript">
			$(function() {
				$('#page').shadow({type:'sides', sides:'vt-2'});
				$('input[type=button], input[type=submit], button').button();
			});
		</script>
	</head>
	<body>
		<div class="container">
			<div id="page">		
				<h1>Generic Migration Tool</h1>
				<h2>Migration Summary</h2>
				<h4><s:actionerror /></h4>
				<s:form action="MigrationSummary" method="POST">
					<tr>
						<td colspan="2">
							<table summary="Cascade Server Information" width="100%">
								<tr><th colspan="2">Cascade Server Information</th></tr>
								<tr><td>Cascade Server URL:</td><td><s:property value="projectInformation.url"/></td></tr>
								<tr><td>Username:</td><td><s:property value="projectInformation.username"/></td></tr>
								<tr><td>Site Name:</td><td><s:property value="projectInformation.siteName"/></td></tr>
							</table>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<table class="mappingReport">
								<tr><th colspan="3">Mapping</th></tr>								
								<s:if test="projectInformation.fieldMapping.size()>0">
									<tr><th colspan="3">XPath</th></tr>
									<s:iterator value="projectInformation.fieldMapping.entrySet()">
										<tr><td><s:property value="key"/></td><td class="arrow">-&gt;</td><td><s:property value="value.label"/></td></tr>
									</s:iterator>
								</s:if>
								<s:if test="projectInformation.staticValueMapping.size()>0">
									<tr><th colspan="3">Static Value</th></tr>
									<s:iterator value="projectInformation.staticValueMapping.entrySet()">
										<tr><td><s:property value="value"/></td><td class="arrow">-&gt;</td><td><s:property value="key.label"/></td></tr>
									</s:iterator>
								</s:if>
							</table>
						</td>
					</tr>
					<s:radio list="overwriteBehaviorList" name="overwriteBehavior" label="Overwrite Behavior"></s:radio>
					<tr>
						<td><button onclick="window.location='/AssignFields?assetType=<s:property value="nAssetTypes-1"/>';return false;">Previous</button></td>
						<td align="right"><input type="submit" value="Go" style="color: green;font-size: 1.5em;" name="submitButton"/></td>
					</tr>
				</s:form>
			</div>
		</div>
	</body>
</html>