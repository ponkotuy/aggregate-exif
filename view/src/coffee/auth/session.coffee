$(document).ready ->
  new Vue
    el: '#session'
    data:
      email: ''
      password: ''
      message: undefined
    methods:
      login: ->
        json = JSON.stringify({email: @email, password: @password})
        fetch('/api/session', {method: 'POST', headers: JSONHeader, body: json, credentials: 'include'}).then (res) =>
          if res.ok
            params = new URLSearchParams(location.search.slice(1))
            location.href = params.get('to') ? '/index.html'
          else
            res.text().then (text) =>
              @message.danger(text)
      setMessage: ->
        @message = vueMessage('#message')
    mounted: ->
      @setMessage()
