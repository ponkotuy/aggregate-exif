$(document).ready ->
  fetch('/api/users?public=true')
    .then (res) -> res.json()
    .then (json) -> render(json)

render = (json) ->
  new Vue
    el: '#users'
    data:
      users: json
