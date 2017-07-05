
@urlParams =
  data:
    params: {}
  methods:
    setParams: ->
      @params = fromURLParameter(location.search.slice(1))
  mounted: ->
    @setParams()

@vueMessage = (el) ->
  new Vue
    el: el
    data:
      message: undefined
      type: undefined
    methods:
      danger: (mes) ->
        @message = mes
        @type = "alert-danger"
      clear: ->
        @message = undefined
        @type = undefined

@copyObject = (src) ->
  $.extend(true, {}, src)

@JSONHeader = {
  'Content-Type': 'application/json'
}

@fromURLParameter = (str) ->
  obj = {}
  for kv in str.split('&')
    ary = kv.split('=')
    key = ary.shift()
    obj[key] = ary.join('=')
  obj

@session = fetch('/api/user', {credentials: 'include'})

@mustSession = ->
  @session.then (res) ->
    unless res.ok
      to = encodeURIComponent("#{location.href}")
      location.href = "/auth/session.html?to=#{to}"
