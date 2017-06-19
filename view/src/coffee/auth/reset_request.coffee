$(document).ready ->
  new Vue
    el: '#request'
    data:
      email: ''
    methods:
      submit: ->
        API.putJSON
          url: '/api/password_reset'
          data: {email: @email}
          success: ->
            location.href = '/'
