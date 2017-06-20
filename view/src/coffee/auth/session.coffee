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
            location.href = fromURLParameter(location.search.slice(1)).redirect ? '/index.html'
          else
            res.text().then (text) =>
              @message.danger(text)
      setMessage: ->
        @message = vueMessage('#message')
    mounted: ->
      @setMessage()
