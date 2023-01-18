/**
 * Login management 
 */

(function () { //creates a scope: avoid variables ending up in the global scope

	var login_button = document.getElementById("loginButton");
	var register_button = document.getElementById("registrationButton");
	var error_registration = document.getElementById("errorMessageRegistration");
	var email = register_button.closest("form").querySelector('input[name="email"]');
	var password = register_button.closest("form").querySelector('input[name="pwd"]');
	var repeated_password = register_button.closest("form").querySelector('input[name="repeated_pwd"]');

	function validateEmail() {  
        
        var re = new RegExp("[a-zA-Z0-9.]+@[a-zA-Z0-9]+\\.[a-zA-Z.]+");
        return re.test(email.value);
        
    }

    login_button.addEventListener('click', (e) => {
		var form = e.target.closest("form");

        if (form.checkValidity()) {
			makeCall("POST", 'Login', form,
                function (x) {
                    if (x.readyState == XMLHttpRequest.DONE) {
                        var message = x.responseText;

                        switch (x.status) {
                            case 200:
                                sessionStorage.setItem("username", message);	
                                window.location.href = "Home.html";
                                break;
                            case 400: //bad reqeust
                            case 401: //unauthorixed
                            case 500: //server error
                                document.getElementById("errorMessageLogin").textContent = message;
                        }
                    }
                }
            );
        } else {
            form.reportValidity();
        }
    });
    
    
    register_button.addEventListener('click', (e) => {
		var form = e.target.closest("form");
		
		if(form.checkValidity()) {
			//Check if mail is syntactically valid 
			if(! validateEmail()) {
				error_registration.textContent = "The mail is not syntactically valid."
				return;
			}
			
			//Check if pwd and repeated pwd are the same
			if(password.value != repeated_password.value) {
				error_registration.textContent = "Password and repeat password are different."; 
				return;
			}
			
			makeCall("POST", 'Registration', form,
                function (x) {
                    if (x.readyState == XMLHttpRequest.DONE) {
                        var message = x.responseText;

                        switch (x.status) {
                            case 200:
                                window.location.href = "index.html";
                                break;
                            case 400: //bad reqeust
                            case 401: //unauthorixed
                            case 500: //server error
                                document.getElementById("errorMessageRegistration").textContent = message;
                        }
                    }
                }
            );
		} else {
            form.reportValidity();
        }
	});
    
})();


