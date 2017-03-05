/**
 * 
 */
var app = new Vue({
	
	el: "#register",
	
	data: {
		registerUrl: "/KindlePocket/bindingData",
		registerFlag: false,
		newUserInfo: {
			userName:'',
			phone:'',
			email:'',
			emailPwd:'',
			kindleEmail:''
		}
	},
	
	methods: {
		register: function() {
			 
			 if(this.newUserInfo.userName.trim() == ''
				 || this.newUserInfo.phone.trim() == ''
				 || this.newUserInfo.email.trim() == ''
				 || this.newUserInfo.emailPwd.trim() == ''
				 || this.newUserInfo.kindleEmail.trim() == '') {
				 
				 alert("信息不完整啊！");
				 return ;
				 
			 } else {
				// alert("ready to register");
				 
				 //jquery
				/* $.post(this.registerUrl,this.newUserInfo,function(result){
					    console.log(result);
					}); */
				 $.post(this.registerUrl, this.newUserInfo)
				    .success(function(result) { 
				    	if(result && result == true) {
				    		alert("注册成功！");
				    	} else {
				    		alert("注册失败！");
				    	}
				    })
				    .error(function(result) { 
				    	console.log(result);
				    	alert("注册失败！"); 
				    });
				 
				 // axios
				/* axios.post(this.registerUrl, this.newUserInfo,{
					 transformRequest: [function (data) {
					    // Do whatever you want to transform the data
					    let ret = ''
					    for (let it in data) {
					      ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
					    }
					    return ret
					  }],
					  headers: {
					    'Content-Type': 'application/x-www-form-urlencoded'
					  }
				  })
				  .then(function (response) {
				    alert("注册成功");
				  })
				  .catch(function (error) {
				    alert("注册失败");
				  });   */
				 
				// vue-resource 用法
				 /*this.$http.post(this.registerUrl, this.newUserInfo, { emulateJSON: true })
				 .then((response) => {
				 console.log(response.data);
				 if(response.data.isBinded == 1) {
				 this.registerFlag = true;
				 alert("注册成功！");
				 }
				 }) */
				 
			 }
			
			
			
		},
		test: function() {
			alert('test function invoked!');
		}
	},
	created: function() {
		//alert('created function invoked!');
	}
})