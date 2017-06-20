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
        location.href = "/auth/session.html?redirect=#{to}"
      logout: ->
        fetch('/api/session', {method: 'DELETE', credentials: 'include'})
          .then -> location.reload(false)
