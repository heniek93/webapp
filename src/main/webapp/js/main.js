$.postJSON = function (url, data, callback) {
   return $.post(url, data, callback, "json");
};

//$.fx.off = true;

var OK = "OK";

var pageUrl = "http://"+window.location.hostname+":8080/skinp";
var ajxURL = pageUrl+"/ajax";
$(document).ready(function() {
	main.hideAll(null,0);
	user.init();
	menu.init();
	zones.init();
	lastEvents.open();
});

var user = {
	init: function() {
		$("#user .signIn input").unbind("keydown").keydown(function(e) {
			if (e.which == 13) {
				user.signIn();
			}
		});
		$("#user .signIn button").click(function() {
			user.signIn();
		});
		if ($("#user").hasClass("signedIn")) {
			user.signedIn();
		}
	},
	panel: {
		load: function() {
			$.postJSON(ajxURL, {
				module: "user",
				getPanel: true
			},function(ajx) {
				if (ajx.status == "OK") {
					user.panel.init(ajx.data);
				}
			});
		},
		init: function(data) {
			$("#user .data .name").html(data.generalInfo.name);
			$("#user .data .email").html(data.generalInfo.email);
			$("#user .data .signOut").fadeIn(0).unbind("click").click(function() {
				user.signOut();
			});
			
			$("#user .zones ul").empty();
			$.each(data.zones,function(key,zone) {
				var ul = $("#user .zones > ul");
				if (zone.parent > 0) {
					var parent = ul.find('li[data-id="'+zone.parent+'"]');
					if (parent.size() > 0) {
						if (!(ul = parent.find("ul")).size()) {
							ul = $('<ul></ul>').appendTo(parent);
						}
					}
				}
				var li = $('<li data-id="'+zone.id+'" data-parent="'+zone.parent+'" data-active="'+(data.generalInfo.zone == zone.id?'1':'0')+'"><span class="name">'+zone.name+'</span></li>').appendTo(ul);
				li.find("> span.name").unbind("click").click(function() {
					console.log("click",li,this);
					user.getIn($(this).closest("li").data("id"));
				});
			});
			if (data.generalInfo.zone) {
				var li = $('<li class="out"><span class="name">Wyjście</span></li>').appendTo("#user .zones > ul").find("> span.name").click(function() {
					user.getOut();
				});
			}
		}
	},
	getIn: function(id) {
		$.postJSON(ajxURL,{
			module: "user",
			getIn: true,
			zone: id
		},function(ajx) {
			if (ajx.status == "OK") {
				user.panel.load();
				lastEvents.load();
			}
		});
	},
	getOut: function() {
		$.postJSON(ajxURL,{
			module: "user",
			getOut: true
		},function(ajx) {
			if (ajx.status == "OK") {
				user.panel.load();
				lastEvents.load();
			}
		});
	},
	signIn: function() {
		var email = $("#user .signIn input.email").val();
		var password = $("#user .signIn input.password").val();
		$.postJSON(ajxURL,{
			module: "user",
			signIn: true,
			email: email,
			password: password
		},function(ajx) {
			if (ajx.status == "OK") {
				user.signedIn();
			} else if (ajx.status == "AccDen") {
				alert("Niepoprawny e-mail lub hasło.");
			}
		});
	},
	signOut: function() {
		$.postJSON(ajxURL,{
			module: "user",
			signOut: true
		},function(ajx) {
			if (ajx.status == "OK") {
				user.signedOut();
			}
		});		
	},
	signedIn: function() {
		user.panel.load();
		$("#user").animate({
			"margin-left": "920px",
			"width": "300px"
		},300,function() {
			$("#user .signIn").slideUp(300);
			$("#user .data").slideDown(300);
			$("#user .zones").slideDown(300);
			$("#menu").fadeIn(300);
			$("#container").fadeIn(300,function() {
				$("body").addClass("signedIn");
			});
		});
		
	},
	signedOut: function() {
		window.location.reload(true);
	}
};

var menu = {
	init: function() {
		$("#menu [data-slug='lastEvents']").click(function() {
			lastEvents.open();
		});
		$("#menu [data-slug='admin-zones']").click(function() {
			zones.open();
		});
		$("#menu [data-slug='reports']").click(function() {
			reports.open();
		});
	}
};

var main = {
	hideAll: function(callback,time) {
		if (typeof time == "undefined" || typeof time == "null") {
			time = 300;
		}
		$("#menu .active").removeClass("active");
		$.when($("#container > div").slideUp(time)).then(function() {
			if (typeof callback == "function") {
				callback.call();
			}
		});
	}
};

var lastEvents = {
	open: function() {
		main.hideAll(function() {
			lastEvents.load(function() {
				$("#lastEvents").slideDown(300);
			});
			$("#menu [data-slug='lastEvents']").addClass("active");
		});
	},
	load: function(callback) {
		$.postJSON(ajxURL,{
			module: "user",
			getLastEvents: true
		},function(ajx) {
			if (ajx.status == OK) {
				var tbody = $("#lastEvents table tbody").empty();
				var i = 0;
				$.each(ajx.data,function(key,event) {
					var action;
					if (event.action == "in") {
						action = "wejście";
					} else {
						action = "wyjście";
					}
					var tr = $('<tr data-id="'+event.id+'"><td>'+event.ts+'</td><td class="zoneName">'+event.zoneName+'</td><td>'+action+'</td></tr>').appendTo(tbody);
					if (i%2 == 1) {
						tr.addClass("odd");
					}
					i++;
				});
				$("#lastEvents table").slideDown(300);
				if (typeof callback == "function") {
					callback.call();
				}
			} else if (ajx.status == "NoRows") {
				$("#lastEvents table").slideUp(300);
			}
		})
	}
};

var zones = {
	init: function () {
		$("#zones > button.add").unbind("click").click(function() {
			zones.add.init();
		});
	},
	add: {
		init: function(parent) {
			if (!parent) {
				var ul = $("#zones > ul");
			} else {
				if (!(ul = parent.find(" > ul")).size()) {
					var ul = $('<ul/>').appendTo(parent);
				}
			}
			var li = $('<li data-id="add"><span class="name">Nowa strefa</span><span class="options"></span></li>').appendTo(ul);
			if (parent) {
				li.data("parent",parent.data("id"));				
			} else {
				li.data("parent",null);
			}
			zones.rename.init(li);
		}
	},
	rename: {
		original: null,
		li: null,
		init: function(li) {
			zones.rename.li = li;
			var span = li.find("> span.name");
			var optionsSpan = li.find("> span.options");
			li.find("> span.options button").fadeOut(0);
			
			zones.rename.original = span.html();
			span.empty();
			var input = $('<input>').appendTo(span).val(zones.rename.original);
			var cancelBtn = $('<button class="cancel">Anuluj</button>').appendTo(optionsSpan).click(function() {
				zones.rename.cancel();
			});
			var saveBtn = $('<button class="save">Zapisz</button>').appendTo(optionsSpan).click(function() {
				zones.rename.execute();
			});
			input.focus().select();
		},
		cancel: function() {
			var li = zones.rename.li;
			if (li.data("id") === "add") {
				li.remove();
			} else {
				li.find("> span.name").html(zones.rename.original);
			}
			zones.rename.li = null;
			zones.rename.original = null;
			li.find("> span.options").find("button.save,button.cancel").remove();
			li.find("> span.options button").fadeIn(200);
		},
		execute: function() {
			var li = zones.rename.li;
			var name = li.find("input").val();
			var parent = li.data("parent");
			if (li.data("id") == "add") {
				var data = {
					module: "zone",
					add: true,
					name: name,
					parent: parent
				};
			} else {
				var id = li.data("id");
				var data = {
					module: "zone",
					rename: true,
					id: id,
					name: name
				};
			}
			
			$.postJSON(ajxURL,data,function(ajx) {
				if (ajx.status == OK) {
					zones.load(true);
				} else if (ajx.status == "Exists") {
					alert("Istnieje już strefa o takiej nazwie.");
				} else if (ajx.status == "Invalid") {
					alert("Nazwa strefy jest nieprawidłowa.");
				} else {
					alert("Wystąpił nieznany błąd.");
					console.log(ajx);
				}
			});
			
		}
	},
	open: function() {
		main.hideAll(function() {
			zones.load(null,true);
		});
		$("#menu [data-slug='admin-zones']").addClass("active");
	},
	remove: function(id) {
		if (confirm("Czy na pewno chcesz usunąć tę strefę?")) {
			$.postJSON(ajxURL,{
				module: "zone",
				remove: true,
				id: id
			},function(ajx) {
				if (ajx.status == OK) {
					zones.load(true);
				}
			});
		}
	},
	load: function(change,slideDown) {
		if (change) {
			user.panel.load();			
		}
		$.postJSON(ajxURL,{
			module: "zone",
			getList: true
		},function(ajx) {
			if (ajx.status == "OK") {
				var ul = $("#zones > ul").empty();
				$.each(ajx.data,function(key,zone) {
					var ul = $("#zones > ul");
					if (zone.parent > 0) {
						
						var parent = $("#zones li[data-id='"+zone.parent+"']");
						if ((ul = parent.find("> ul")).size() == 0) {
							ul = $('<ul/>').appendTo(parent);
						}
					}
					var li = $('<li data-id="'+zone.id+'"><span class="name">'+zone.name+'</span><span class="options"></span></li>').appendTo(ul);
					var options = li.find("span.options");
					if (zone.parent == 0) {
						$('<button>Dodaj podstrefę</button>').appendTo(options).click(function() {
							zones.add.init(li);
						});
					}
					$('<button>Zmień nazwę</button>').appendTo(options).click(function() {
						zones.rename.init(li);
					});
					$('<button>Usuń</button>').appendTo(options).click(function() {
						zones.remove(li.data("id"));
					});
					
				});
				$("#zones").slideDown(300);
			}
		});
	}
}

var reports = {
	open: function() {
		main.hideAll(function() {
			$.when(reports.zones.init(),reports.users.init()).done(function() {
				$("#reports").slideDown(300);				
			});
		});
		$("#menu [data-slug='reports']").addClass("active");
	},
	zones: {
		init: function(callback) {
			var date = new Date();
			$("#reportsZones div.options input.date").datepicker({
				dateFormat: "yy-mm-dd",
				onSelect: function() {
					reports.zones.load();					
				},
				constrainInput: true
			});
			$("#reportsZones div.options input.date").val(date.getFullYear()+"-"+leadingZeros(date.getMonth()+1)+"-"+leadingZeros(date.getDate()));
			$("#reportsZones div.options input.time").val(leadingZeros(date.getHours())+":"+leadingZeros(date.getMinutes())+":"+leadingZeros(date.getSeconds()));
			$("#reportsZones div.options input").unbind("keydown").keydown(function(e) {
				if (e.which == 13) {
					reports.zones.load(callback);
				}
			});
			reports.zones.load();
		},
		load: function(callback) {
			var date = $("#reportsZones div.options input.date").val();
			var time = $("#reportsZones div.options input.time").val();
			if (time.length == 5) {
				time += ":00";
			}
			$.postJSON(ajxURL,{
				module: "report",
				getZonesReport: true,
				datetime: date+" "+time
			},function(ajx) {
				if (ajx.status == OK) {
					var ul = $("#reportsZones ul").empty();
					var lastZone = null;
					var lastZoneUsers = 0;
					var lastZoneLi = null;
					var fn = function() {
						if (lastZone != null) {
							lastZoneLi.find("> span.count").html(lastZoneUsers+" "+odmien(lastZoneUsers,"pracownik|pracowników|pracowników")).unbind("click").click(function() {
								$(this).siblings("ul.users").slideToggle(300);
							});
							if (lastZoneUsers > 0) {
								lastZoneLi.find("> span.count").addClass("active");
							}
							lastZoneLi.find("ul.users").slideUp(0);
							lastZoneUsers = 0;
						}
					}
					$.each(ajx.data,function(key,row) {
						if (lastZone != row.id) {
							fn();
							ul = $("#reportsZones > ul");
							if (row.parent > 0) {
								if (!(ul = $("#reportsZones li[data-id='"+row.parent+"'] > ul.children")).size()) {
									ul = $('<ul class="children"/>').appendTo($("#reportsZones li[data-id='"+row.parent+"']"));
								}
							}
							var li = $('<li data-id="'+row.id+'"><span class="name">'+row.zoneName+'</span><span class="count"></span><ul class="children"></ul><ul class="users"></ul></li>').appendTo(ul);
							lastZone = row.id;
							lastZoneLi = li;
							li.find("span.count").qtip({
								content: "Rozwiń/zwiń użytkowników",
								position: {
									my: "left center",
									at: "right center"
								},
								style: {
									classes: "qtip-tipsy"
								}
							});
						}
						if (row.userName) {
							var user = $('<li><span class="name">'+row.userName+'</span><span class="email">('+row.email+')</span></li>').appendTo(lastZoneLi.find("ul.users"));
							lastZoneUsers++;
						}
					});
					fn();
					if (typeof callback == "function") {
						callback.call();						
					}
				}
			});
		}
	},
	users: {
		init: function(callback) {
			$("#reportsUsers div.options input.from").datepicker({
				dateFormat: "yy-mm-dd",
				onSelect: function() {
					reports.users.loadUserList();					
				},
				constrainInput: true
			});
			$("#reportsUsers div.options input.to").datepicker({
				dateFormat: "yy-mm-dd",
				onSelect: function() {
					reports.users.loadUserList();					
				},
				constrainInput: true
			});
			reports.users.loadUserList();
		},
		loadUserList: function() {
			var select = $("#reportsUsers div.options select.user").html('<option>Ładowanie...</option>').prop("disabled",true);
			$.postJSON(ajxURL,{
				module: "user",
				getList: true
			},function(ajx) {
				if (ajx.status == "OK") {
					select.empty().prop("disabled",false);
					$.each(ajx.data,function(key,row) {
						var option = $('<option value="'+row.id+'"'+((row.signedIn===true)?' selected':'')+'>'+row.name+' ('+row.email+')</option>').appendTo(select);
					});
					select.unbind("change").change(function() {
						reports.users.load();
					});
					reports.users.load();
				}
			})
		},
		load: function(callback) {
			var userID = $("#reportsUsers div.options select.user").val();
			var from = $("#reportsUsers div.options .from").val();
			var to = $("#reportsUsers div.options .to").val();
			from += " 00:00:00";
			to += " 23:59:59";
			$.postJSON(ajxURL,{
				module: "report",
				getEventsTimeReport: true,
				from: from,
				to: to,
				userID: userID
			},function(ajx) {
				if (ajx.status == OK) {
					var tbody = $("#reportsUsers table tbody").empty();
					$.each(ajx.data.events,function(key,row) {
						var tr = $('<tr data-id="'+row.id+'"><td>'+row.ts+'</td><td class="zoneName">'+row.zoneName+'</td><td>'+row.action+'</td></tr>').prependTo(tbody);
					});
					$("#reportsUsers .totalTime").html(ajx.data.time);
					$("#reportsUsers table, #reportsUsers .time").slideDown(300);
					if (typeof callback == "function") {
						callback.call();						
					}
				} else if (ajx.status == "NoRows") {
					$("#reportsUsers table, #reportsUsers .time").slideUp(300);
				}
			});
		}
	}
}

function leadingZeros(value) {
	if (value < 10) {
		return "0"+value;
	} else {
		return value;
	}
}

function odmien(count, options) {
	options = options.split("|");
	if (count%10 > 4 || (count%100 > 10 && count%100 <= 21) || count%10 == 0) {
		return options[2];
	} else if (count%10 > 1) {
		return options[1];
	} else if (count == 1) {
		return options[0];
	}
}