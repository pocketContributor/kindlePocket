angular.module('infoUpdate',[])
.controller('infoUpdateController',function($scope,$http){

    $scope.subscriberData = {},

    $http({
        method: 'GET',
        url: 'http://0e5662da.ngrok.io/Weixin/getSubscriberInfo?subscriberOpenId=ojXQbxLbEY9wnrFgnL5ODCyVVpnc'
    }).success(function(data,status){
        $scope.subscriberData.openId = data.id;
        $scope.subscriberData.userName = data.userName;
        $scope.subscriberData.phone = data.phone;
        $scope.subscriberData.email = data.email;
        $scope.subscriberData.kindleEmail = data.kindleEmail;
        alert("findInfo successfully! id= " + data.id);
    }).error(function(data,status){
        alert("findInfo error! data" + data + " status:" + status);
    })

    $scope.updateInfo = function(){
          $http({
              method: 'POST',
              url: 'http://0e5662da.ngrok.io/Weixin/updateSubscriberInfo',
              data: $.param($scope.subscriberData),
              headers : { 'Content-Type': 'application/x-www-form-urlencoded' }
          }).success(function(data,status){
              alert("update successfully!")
          }).error(function(data,status){
              alert("update error!")
          })
    }

});