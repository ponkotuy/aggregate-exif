$(document).ready ->
  params = fromURLParameter(location.search.slice(1))
  console.log(params)
  new Vue
    el: '#password'
    data:
      password: ''
      retype: ''
    methods:
      submit: ->
        if @password == @retype
          API.deleteJSON
            url: '/api/password_reset'
            data: {password: @password, secret: params.secret}
            success: ->
              location.href = '/auth/session.html'
        else
          window.alert('Passwordが一致しません')
