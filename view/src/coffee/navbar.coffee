$(document).ready ->
  session.then (res) ->
    if res.ok
      res.json()
        .then (json) -> render(json)
    else
      render(undefined)

render = (session) ->
  new Vue
    el: '#navs'
    data:
      session: session
    methods:
      login: ->
        to = encodeURI("#{location.pathname}#{location.search}")
        location.href = "/auth/session.html?to=#{to}"
      logout: ->
        fetch('/api/session', {method: 'DELETE', credentials: 'include'})
          .then -> location.reload(false)
      public: (flag) ->
        res = if(flag)
          fetch('/api/user/public', {method: 'PUT', credentials: 'include'})
        else
          fetch('/api/user/private', {method: 'PUT', credentials: 'include'})
        res.then ->
          location.reload(false)
