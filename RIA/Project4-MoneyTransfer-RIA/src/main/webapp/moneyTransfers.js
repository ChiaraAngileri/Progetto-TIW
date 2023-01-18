/**
 * Money transfers
 */

{   //avoid variables ending up in the global scope

    //page components
    let bankAccountsList, moneyTransfersList, personalMessage, transferResult, addressBook,
        pageOrchestrator = new PageOrchestrator();  //main controller

    window.addEventListener("load", () => {
        if (sessionStorage.getItem("username") == null) {   //not logged
            window.location.href = "index.html";
        } else {    //logged
            pageOrchestrator.start(); //initialize the components
            pageOrchestrator.refresh();
        } //display initial content
    }, false);


    //Constructors of view components

	function PageOrchestrator() {
        var alertContainer = document.getElementById("id_alert");

        this.start = function () {
            personalMessage = new PersonalMessage(sessionStorage.getItem("username"), document.getElementById("id_username"), document.getElementById("logout_button"));
            personalMessage.show();

            bankAccountsList = new BankAccountsList(alertContainer,
                document.getElementById("id_bankAccounts"), document.getElementById("id_bankAccountsBody"),
                document.getElementById("id_createAccountForm"), document.getElementById("createAccountButton"), document.getElementById("create_account_warning"));

            moneyTransfersList = new MoneyTransfersList({ //many parameters, wrap them in an object
                alert: alertContainer,

                accountDetails: document.getElementById("id_bankAccountDetails"),
                accountName: document.getElementById("id_accountName"),
                accountID: document.getElementById("id_accountID"),
                accountBalance: document.getElementById("id_accountBalance"),

                moneyTransferForm: document.getElementById("id_moneyTransferForm"),
                makeTransferButton: document.getElementById("makeTransferButton"),
                
                resetTransferButton: document.getElementById("resetMakeTransferButton"),

                transferList: document.getElementById("id_transferList"),
                transfersBody: document.getElementById("id_transfersBody")
            });

            transferResult = new TransferResult({ //many parameters, wrap them in an object
                success_div: document.getElementById("success"),
                failure_div: document.getElementById("failure"),

                amount_span: document.getElementById("id_amountSuccess"),
                reason_span: document.getElementById("id_reasonSuccess"),

                srcAccName_td: document.getElementById("id_srcAccName"),
                srcAccID_td: document.getElementById("id_srcAccID"),
                originalAmountSrc_td: document.getElementById("id_originalAmountSrc"),
                srcAccBalance_td: document.getElementById("id_srcAccBalance"),

                destAccName_td: document.getElementById("id_destAccName"),
                destAccID_td: document.getElementById("id_destAccID"),
                originalAmountDest_td: document.getElementById("id_originalAmountDest"),
                destAccBalance_td: document.getElementById("id_destAccBalance"),

                failureTransferMessage_span: document.getElementById("failureTransferMessage")
            });

            addressBook = new AddressBook(document.getElementById("addContactButton"), document.getElementById("infoAddressBook"), 
            				document.getElementById("dest_sugg"), document.getElementById("userDest"), 
            				document.getElementById("accDest_sugg"));

            document.querySelector("a[href='Logout']").addEventListener("click", () => { //si aggiunge event listener ad un'ancora in modo che anche lato js faccia qualcosa
                window.sessionStorage.removeItem("username");
            })
                        
        };

        this.refresh = function (currentBankAccount) { //currentBankAccount initially null
            alertContainer.textContent = "";

            bankAccountsList.reset();
            moneyTransfersList.reset();
            bankAccountsList.show(function () {
                bankAccountsList.autoclick(currentBankAccount);
            }); //closure preserves visibility of this
            addressBook.load();
            transferResult.reset();
        };
    }
    

    function PersonalMessage(_username, messageContainer, _logout_button) { 
        this.username = _username;
        this.logoutButton = _logout_button;
        
        this.logoutButton.addEventListener("click", () => {
			sessionStorage.clear();
		});
        
        this.show = function () {
            messageContainer.textContent = this.username;
        }
    }


    function BankAccountsList(_alert, _bankAccounts, _bankAccountsBody, _createAccountForm, _createAccountButton, _createAccountWarning) {
        this.alert = _alert;
        this.bankAccounts = _bankAccounts;
        this.bankAccountsBody = _bankAccountsBody;
        this.createAccountForm = _createAccountForm;
        this.createAccountButton = _createAccountButton;
        this.createAccountWarning = _createAccountWarning;

        this.accounts = [];
        
        this.createAccountButton.addEventListener("click", (e) => {
            var form = e.target.closest("form");

            if (form.checkValidity()) {
                var self = this;
                var name = form.querySelector("input[name='nameAccount']");

				//check if the user already has an account with this name
                if (self.accounts.includes(name.value)) { 
                    self.createAccountWarning.textContent = "Chosen account name already exists.";
                    return;
                }

                makeCall("POST", "CreateBankAccount", form,
                    function (req) {
                        if (req.readyState = 4) {
                            var message = req.responseText;

                            if (req.status == 200) {
								if(message != "") {
                                	pageOrchestrator.refresh(message);  //id of the new account
                                }                           
                            } else if (req.status == 403) {
                                window.location.href = req.getResponseHeader("Location");
                                window.sessionStorage.removeItem("username");
                            } else {
                                self.createAccountWarning.textContent = message;
                            }
                        }
                    }
                );
            } else {
                form.reportValidity();
            }
        });

        this.show = function (next) {
            var self = this;

            makeCall("GET", "GetAccountsData", null,
                function (req) {
                    if (req.readyState == 4) {
                        var message = req.responseText;

                        if (req.status == 200) {
                            var bankAccountsToShow = JSON.parse(req.responseText);

                            if (bankAccountsToShow.lenght == 0) {
                                self.alert.textContent = "No bank accounts.";
                                return;
                            }

                            self.update(bankAccountsToShow); //self visible by closure

                            if (next) next(); //show the default element of the list if present
                            

                        } else if (req.status == 403) {
                            window.location.href = req.getResponseHeader("Location");
                            window.sessionStorage.removeItem("username");

                        } else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        };

        this.update = function (arrayAccounts) {
            this.bankAccountsBody.innerHTML = ""; //empty the table body

            //build updated list
            var self = this;

            arrayAccounts.forEach(function (account) {  //self visible here, not this
                var row = document.createElement("tr");
                var name = document.createElement("td");
                
                var anchor = document.createElement("a");
                name.appendChild(anchor);
                linkText = document.createTextNode(account.name);
                anchor.appendChild(linkText);
                
                anchor.setAttribute("accountID", account.id); //set a custom HTML attribute
                anchor.addEventListener("click", (e) => {
                    moneyTransfersList.show(e.target.getAttribute("accountID")); 
                }, false);
                anchor.href = "#";

                row.appendChild(name);
                
                self.bankAccountsBody.appendChild(row);

                self.accounts.push(account.name);
            });
            
            this.bankAccounts.style.visibility = "visible";

        };

        this.autoclick = function (accountID) {
            var e = new Event("click");
            var selector = "a[accountid='" + accountID + "']";
            var anchorToClick = (accountID) ? document.querySelector(selector) //account with id = accountID
                : this.bankAccountsBody.querySelectorAll("a")[0]; //the first account                
            if (anchorToClick) anchorToClick.dispatchEvent(e);
        };

        this.reset = function () {
            this.bankAccounts.style.visibility = "hidden";
            this.createAccountForm.reset();
        };

    }


    function MoneyTransfersList(options) { 
        this.alert = options['alert'];

        this.accountDetails = options['accountDetails'];
        this.accountName = options['accountName'];
        this.accountID = options['accountID'];
        this.accountBalance = options['accountBalance'];

        this.moneyTransferForm = options['moneyTransferForm'];
        this.makeTransferButton = options['makeTransferButton'];
        
        this.resetTransferButton = options['resetTransferButton'];
        
        this.dest_input = this.moneyTransferForm.querySelector("input[name='userDest']");
        this.acc_input = this.moneyTransferForm.querySelector("input[name='idAcc_dest']");

        this.transferList = options['transferList'];
		this.transfersBody = options['transfersBody'];


		//Reset form		
		this.resetTransferButton.addEventListener("click", (e) => {
			var form = e.target.closest("form");
			form.reset();
		})

		//Make transfer form		
        this.makeTransferButton.addEventListener("click", (e) => {
            var form = e.target.closest("form");

            if (form.checkValidity()) {
	
                var accDestID = form.querySelector("input[name='idAcc_dest']");
                var accSrcID = form.querySelector("input[name='idAcc_src']");
                var amount = form.querySelector("input[name='amount']");
                

                //Check src and dest account
                if (accDestID.value == accSrcID.value) {
					//form.reset();
                    transferResult.showFailure("You can't make a transfer between the same account.");
                    return;
                }

                
				//Check enough money
				if(Number(amount.value) > Number(this.accountBalance.textContent)){
					transferResult.showFailure("You don't have enough money to do this transfer.");
					return;
				}
				
				makeCall("POST", "MakeTransfer", form,
                        function (req) {
                            if (req.readyState == 4) {
                                var message = req.responseText;
                                
                                if (req.status == 200) {
									var data = JSON.parse(message);	
                                    pageOrchestrator.refresh(data.srcAccount.id);
                                    transferResult.showSuccess(data.srcAccount, data.destAccount, data.transfer);
                                } else if (req.status == 403) {
                                    window.location.href = req.getResponseHeader("Location");
                                    window.sessionStorage.removeItem("username");
                                } else {
                                    transferResult.showFailure(message);
                                }
                            }
                        }
                    );
                } else {
                    form.reportValidity();
                }
        });

        //AUTOCOMPLETAMENTO
        //L'evento focus su un elemento del form è quell'evento che si attiva nel momento in cui il campo diventa “attivo”; 
        //nel caso di una textarea o di un campo di input, ad esempio, si verifica l'evento focus nel momento in cui il campo diventa compilabile.

        //L’evento keyup scaturisce quando un tasto viene rilasciato.
        this.dest_input.addEventListener("focus", (e) => {
            addressBook.autoCompleteNameDest(e.target.value);
        });
        this.dest_input.addEventListener("keyup", (e) => {
            addressBook.autoCompleteNameDest(e.target.value);
        });
        
        this.acc_input.addEventListener("focus", () => {
			addressBook.autoCompleteAccountDest();
		});
        
        this.show = function (bankAccountID) {
            var self = this;
            makeCall("GET", "GetBankAccountDetailsData?bankAccountID=" + bankAccountID, null,
                function (req) {
                    if (req.readyState == 4) {
                        var message = req.responseText;
                        if (req.status == 200) {
                            var data = JSON.parse(req.responseText);
                            self.update(data.bankAccount, data.moneyTransfers); //self is the object on which the function is applied
                            self.accountDetails.style.visibility = "visible";
                            self.transferList.style.visibility = "visible";
                            self.moneyTransferForm.style.visibility = "visible";
                            self.moneyTransferForm.idAcc_src.value = bankAccountID;
                        } else if (req.status == 403) {
                            window.location.href = req.getResponseHeader("Location");
                            window.sessionStorage.removeItem("username");
                        } else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        };

        this.update = function (bA, mT) {
            this.accountName.textContent = bA.name;
            this.accountID.textContent = bA.id;
            this.accountBalance.textContent = bA.balance;
            
            var self = this;
            
            this.transfersBody.innerHTML = ""; //empty the table body

            if (mT.length == 0) {
                this.transfersBody.textContent = "No money transfers for this account."; 
                return;
            }

            mT.forEach((transfer) => {
	
                var row = document.createElement("tr");

                var date = document.createElement("th");
                var origin = document.createElement("th");
                var destination = document.createElement("th");
                var amount = document.createElement("th");
                var reason = document.createElement("th");

                var namecell1 = document.createTextNode(transfer.date);
                var namecell2 = document.createTextNode(transfer.bankAccountSrcId);
                var namecell3 = document.createTextNode(transfer.bankAccountDestId);
                var namecell4 = document.createTextNode(transfer.amount);
                var namecell5 = document.createTextNode(transfer.reason);

                row.className = transfer.bankAccountSrcId == bA.id ? "negative" : "positive";

                date.appendChild(namecell1);
                origin.appendChild(namecell2);
                destination.appendChild(namecell3);
                amount.appendChild(namecell4);
                reason.appendChild(namecell5);

                row.appendChild(date);
                row.appendChild(origin);
                row.appendChild(destination);
                row.appendChild(amount);
                row.appendChild(reason);

                self.transfersBody.appendChild(row);
            });
        };

        this.reset = function () {
            this.accountDetails.style.visibility = "hidden";
            this.transferList.style.visibility = "hidden";
            this.moneyTransferForm.reset();
            this.moneyTransferForm.style.visibility = "hidden";
        };

    }
    

    function TransferResult(options) {

        this.success_div = options["success_div"];
        this.failure_div = options["failure_div"];

        this.amount_span = options['amount_span'];
        this.reason_span = options['reason_span'];

        this.srcAccName_td = options['srcAccName_td'];
        this.srcAccID_td = options['srcAccID_td'];
        this.originalAmountSrc_td = options['originalAmountSrc_td'];
        this.srcAccBalance_td = options['srcAccBalance_td'];

        this.destAccName_td = options['destAccName_td'];
        this.destAccID_td = options['destAccID_td'];
        this.originalAmountDest_td = options['originalAmountDest_td'];
        this.destAccBalance_td = options['destAccBalance_td'];

        this.failureTransferMessage_span = options['failureTransferMessage_span'];

        this.showSuccess = function (src, dest, transfer) {

            this.amount_span.textContent = transfer.amount;
            this.reason_span.textContent = transfer.reason;

            this.srcAccName_td.textContent = src.name;
            this.srcAccID_td.textContent = src.id;
            this.originalAmountSrc_td.textContent = transfer.origin_initialAmount;
            this.srcAccBalance_td.textContent = src.balance;


            this.destAccName_td.textContent = dest.name;
            this.destAccID_td.textContent = dest.id;
            this.originalAmountDest_td.textContent = transfer.destination_initialAmount;
            this.destAccBalance_td.textContent = dest.balance;

            this.success_div.style.visibility = "visible";
            this.failure_div.style.visibility = "hidden";

            addressBook.showAddContactButton(dest.userId);
        };

        this.showFailure = function (reason) {

            this.failureTransferMessage_span.textContent = reason;

            this.failure_div.style.visibility = "visible";
            this.success_div.style.visibility = "hidden";
        };

        this.reset = function () {
            this.success_div.style.visibility = "hidden";
            this.failure_div.style.visibility = "hidden";
            addressBook.reset();
        };
    }


    function AddressBook(_addContact, _infoAddressBook, _dest_sugg, _user_dest, _acc_sugg) {

        this.addContactButton = _addContact;
        this.infoAddressBook = _infoAddressBook;
        this.dest_sugg = _dest_sugg;
        this.user_dest = _user_dest;
        this.acc_sugg = _acc_sugg;
        
        var srcUserID;
        var destUserID;

        this.contacts = [];
        this.accountDest = [];
        this.dest = [];

        var self = this;

        this.load = function () {
            makeCall("GET", "GetContacts", null,
                function (req) {
                    if (req.readyState == 4) {
                        var message = JSON.parse(req.responseText);

                        if (req.status == 200) {
                            self.contacts = message.addressBook;
                            self.accountDest = message.contactToAccounts;
                        } else if (req.status == 403) {
                            window.location.href = req.getResponseHeader("Location");
                            window.sessionStorage.removeItem("username");
                        } else {
                            self.infoAddressBook.textContent = req.responseText;
                        }
                    }
                }
            );

            self.getDestinationUsers();
        };      

        this.showAddContactButton = function (_destUserID) {

			destUserID = _destUserID;

            if (self.contacts.listContactID.includes(destUserID)) {
                self.addContactButton.style.visibility = "hidden";
                self.infoAddressBook.textContent = "";
                return;
            }
                                   
            self.addContactButton.style.visibility = "visible";
        };


        this.addContactButton.addEventListener("click", () => {
            self.addContact(srcUserID, destUserID);
        });

        this.addContact = function (srcUserID, destUserID) {
            //Create form data
            var data = new FormData();
            data.append("ownerID", srcUserID);
            data.append("destID", destUserID);

            makeCall("POST", "AddContact", data,
                function (req) {
                    if (req.readyState == 4) {
                        var message = req.responseText;
                        if (req.status == 200) {
                            self.load();
                            
                            self.infoAddressBook.textContent = "Contact added to the address book!";
            				this.addContactButton.style.visibility = "hidden";
                            
                        } else if (req.status == 403) {
                            window.location.href = req.getResponseHeader("Location");
                            window.sessionStorage.removeItem("username");
                        } else {
                            self.infoAddressBook.textContent = message;
                        }
                    }
                }
            );
        };

        this.getDestinationUsers = function () {

            makeCall("GET", "GetDestinations", null,
                function (req) {
                    if (req.readyState == 4) {
                        var message = req.responseText;
                        if (req.status == 200) {
                            self.dest = JSON.parse(req.responseText);
                        } else if (req.status == 403) {
                            window.location.href = req.getResponseHeader("Location");
                            window.sessionStorage.removeItem("username");
                        } else {
                            self.infoAddressBook.textContent = message;
                        }
                    }
                }
            );
        };

        this.autoCompleteNameDest = function (destUserName) {
	
			//Clear suggestions
            this.dest_sugg.innerHTML = "";
            
            //Get dest match
            if(!self.dest.includes(destUserName)){  
                //suggestions
                let similarDest = [];
                self.dest.forEach(dest => {
                    if (String(dest).startsWith(destUserName)) {
                        similarDest.push(dest);
                    }
                });

                similarDest.forEach(dest => {
                    let optionDest = document.createElement("option");
                    optionDest.text = dest;
                    optionDest.value = dest;
                    this.dest_sugg.appendChild(optionDest);
                });
            }
        };

		this.autoCompleteAccountDest = function(){
			var userDestination = this.user_dest.value;
			var accounts = self.accountDest[userDestination];
			
			//Clear suggestions
			this.acc_sugg.innerHTML = "";
			
			if(accounts != null) {
				accounts.forEach(account => {
	               let optionAcc = document.createElement("option");
	               optionAcc.text = account;
	               optionAcc.value = account;
	               this.acc_sugg.appendChild(optionAcc);
				})
			}			
        };

        this.reset = function () {
            this.addContactButton.style.visibility = "hidden";
        };
    }

}