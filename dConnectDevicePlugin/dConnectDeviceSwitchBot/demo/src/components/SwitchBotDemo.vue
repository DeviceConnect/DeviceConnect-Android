<template>
  <div class="container">
    <div class="row">
      <div class="title mx-auto">SwitchBot DemoApp for JS</div>
    </div>
    <div class="row">
        <div class="col-4 offset-1 text-left">address</div>
        <input class="address col-6" type="text" v-model="address">
    </div>    
    <div class="row">
      <div class="col-4 offset-1 text-left">port</div>
      <input class="port col-6" type="text" v-model="port">
    </div>
    <div class="row">
      <div class="col-4 offset-1 text-left">device list</div>
      <select size=5 class="col-6" v-model="selected">
        <option v-for="device in devices" v-bind:key="device.id" :value="device">
          {{ device.name }}
        </option>
      </select>
    </div>
    <div class="row">
      <div class="col-4 offset-1 text-left">button profile</div>
      <input type="button" value="push" class="col-2" @click="onClickPush">
      <input type="button" value="down" class="col-2" @click="onClickDown">
      <input type="button" value="up" class="col-2" @click="onClickUp">
    </div>
    <div class="row">
      <div class="col-4 offset-1 text-left">switch profile</div>
      <input type="button" value="turn on" class="col-3" @click="onClickTurnOn">
      <input type="button" value="turn off" class="col-3" @click="onClickTurnOff">
    </div>
  </div>
</template>

<script>
export default {
  name: 'SwitchBotDemoAppJS',
  data() {
    return {
      address: undefined,
      port: undefined,
      accessToken: undefined,
      devices: new Array(),
      selected: undefined,
      execute: false,
      handle: undefined,
    }
  },
  mounted() {
    if(localStorage.address) {
      this.address = localStorage.address
    } else {
      this.address = 'localhost'
    }
    if(localStorage.port) {
      this.port = localStorage.port
    } else {
      this.port = '4035'
    }
    if(localStorage.accessToken) {
      this.accessToken = localStorage.accessToken
    }
    this.dConnectDiscoverDevices()
    this.handle = window.setInterval(function(){
      this.dConnectDiscoverDevices()
    }.bind(this), 10000)
  },
  watch: {
    address(newAddress) {
      localStorage.address = newAddress
    },
    port(newPort) {
      localStorage.port = newPort
    },
    accessToken(newAccessToken) {
      localStorage.accessToken = newAccessToken
    },
  },
  methods: {
    onClickPush: function() {
      window.console.log('onClickPush()')
      this.dConnectPost('button', 'push')
    },
    onClickDown: function() {
      window.console.log('onClickDown()')
      this.dConnectPost('button', 'down')
    },
    onClickUp: function() {
      window.console.log('onClickUp()')
      this.dConnectPost('button', 'up')
    },
    onClickTurnOn: function() {
      window.console.log('onClickTurnOn()')
      this.dConnectPost('switch', 'turnOn')
    },
    onClickTurnOff: function() {
      window.console.log('onClickTurnOff()')
      this.dConnectPost('switch', 'turnOff')
    },
    dConnectDiscoverDevices : function() {
      window.console.log('dConnectDiscoverDevices()')
      window.console.log('address:' + this.address)
      window.console.log('port:' + this.port)
      window.dConnect.setHost(this.address)
      window.dConnect.setPort(this.port)
      window.dConnect.discoverDevices(this.accessToken,
        function(response){
          window.console.log('response:' + response)
          var selected = this.selected
          this.devices.splice(0)
          this.selected = undefined
          for(var i = 0; i < response.services.length; i++) {
            window.console.log('service.name:' + response.services[i].name)
            window.console.log('service.id:' + response.services[i].id)
            if(response.services[i].id.indexOf('SwitchBot.Device') != -1) {
              this.devices.push({
                name: response.services[i].name.replace('(SwitchBotDevice)', ''),
                id: response.services[i].id
              })
            }
          }
          if(selected != undefined) {
            window.console.log('selected:' + JSON.stringify(selected))
            for(var j = 0; j < this.devices.length; j++) {
              if(this.devices[j].id == selected.id && this.devices[j].name == selected.name) {
                this.selected = this.devices[j]
              }
            }
          }
        }.bind(this),
        function(errorCode, errorMessage) {
          window.console.log('dConnectDiscoverDevicesFailure() errorCode:' + errorCode + ', errorMessage:' + errorMessage)
          if(errorCode == 12 || errorCode == 13 || errorCode == 15) {
            this.dConnectAuthorization()
          }
        }.bind(this))
    },
    dConnectAuthorization : function() {
      window.console.log('dConnectAuthorization()')
      var scopes = Array("button", "switch", "serviceinformation", "servicediscovery")
      window.dConnect.authorization(scopes, "SwitchBotDemo",
        function(clientId, accessToken) {
          window.console.log('dConnectAuthorizationSuccess() clientId:' + clientId + ', accessToken:' + accessToken)
          this.accessToken = accessToken
          this.dConnectDiscoverDevices()
        }.bind(this),
        function(errorCode, errorMessage) {
          window.console.log('dConnectAuthorizationFailure() errorCode:' + errorCode + ', errorMessage:' + errorMessage)
        }.bind(this))
    },
    dConnectPost: function(profile, attribute) {
      if(this.selected != undefined && this.execute == false) {
        this.execute = true
        window.console.log('dConnectPost() profile:' + profile + ', attribute:' + attribute)
        var uriBuilder = new window.dConnect.URIBuilder()
        uriBuilder.setProfile(profile)
        uriBuilder.setAttribute(attribute)
        uriBuilder.setServiceId(this.selected.id)
        var formData = new FormData()
        formData.append('serviceId', this.selected.id)
        if(localStorage.accessToken) {
          formData.append('accessToken', localStorage.accessToken)
        }
        window.dConnect.post(uriBuilder.build(), undefined, formData,
          function(response) {
            window.console.log('dConnectPostSuccess() response:' + JSON.stringify(response))
            this.execute = false
          }.bind(this),
          function(errorCode, errorMessage) {
            window.console.log('dConnectPostFailure() errorCode:' + errorCode + ', errorMessage:' + errorMessage)
            window.alert('ERROR(errorCode:' + errorCode + ', errorMessage:' + errorMessage + ')')
            this.execute = false
          }.bind(this))
      }
    },
  },
  beforeDestroy() {
    window.console.log('beforeDestroy()')
    if(this.handle) {
      window.clearInterval(this.handle)
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.row {
  margin-bottom: 10px;
}
.title {
  font-size: 24px
}
</style>
