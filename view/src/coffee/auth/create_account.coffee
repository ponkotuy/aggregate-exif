$(document).ready ->
  new Vue
    el: '#createAccount'
    data:
      name: ''
      email: ''
      password: ''
      retype: ''
      message: undefined
    methods:
      submit: ->
        if @retype != @password
          @message.danger('パスワードが一致しません')
          return
        if @password.length < 8
          @message.danger('passwordは8文字以上が必須です')
          return
        json = JSON.stringify({name: @name, email: @email, password: @password})
        fetch('/api/user', {method: 'POST', headers: JSONHeader, body: json}).then (res) =>
          if res.ok
            @alert = ''
            location.href = '/auth/session.html'
          else
            res.text().then (text) =>
              @message.danger(text)
    mounted: ->
      @message = vueMessage('#message')
