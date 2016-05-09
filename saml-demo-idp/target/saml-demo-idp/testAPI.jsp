<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<script src="//code.jquery.com/jquery.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		
		$("#testKeepAlive").click(function() {
			$.ajax({
				url : "http://localhost:7070/saml/keepalive",
				type : "GET",
				success : function() {
					alert(1);
				}
			});
		});

		$("#logoutSSO").click(function(){
			$("body").append('<iframe src="http://localhost:7070/faces/logout_call.xhtml"></iframe>');
		});

        $('#decodeBtn').click(function(){
            var resultXML = atob(unescape(encodeURIComponent(document.getElementById("SAMLCode").value)));
            document.getElementById("SAMLCode").value = resultXML;
        });



	});
</script>

<body>

	


	<Button id="logoutSSO">logoutSSO</Button>



    <textarea class="form-control" name="SAMLCode" id="SAMLCode"
              rows="15"></textarea>
    <Button id="decodeBtn">Decode</Button>
</body>
</html>
