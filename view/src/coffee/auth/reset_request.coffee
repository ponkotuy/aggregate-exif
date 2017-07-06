$(document).ready ->
  new Vue
    el: '#request'
    data:
      email: ''
    methods:
      submit: ->
        option =
          method: 'PUT'
          headers:
            'Content-Type': 'aplication/json'
          body: JSON.stringify({email: @email})
        fetch('/api/password_reset', option)
          .then ->
            location.href = '/'
