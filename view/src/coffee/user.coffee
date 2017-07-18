$(document).ready ->
  document.getElementById('graphTab').className = 'active'
  params = new URLSearchParams(location.search.slice(1))
  renderFilters(params)
  if params.get('userId')
    render(params)
  else
    session.then (res) ->
      if res.ok
        res.json()
          .then (json) ->
            addParams({userId: json.id})
      else
        uri = encodeURIComponent(location.href)
        location.href = "/auth/session.html?to=#{uri}"
  renderPeriod(params)

renderPeriod = (params) ->
  new Vue
    el: '#period'
    data:
      start: params.get('start') || ''
      end: params.get('end') || ''
    methods:
      set: ->
        console.log(@start, @end)
        params = {start: @start || undefined, end: @end || undefined}
        addParams(params)
    mounted: ->
      $('.datetimepicker').datetimepicker({format: "YYYY-MM-DDTHH:mm"})
        .on 'dp.change', (e) =>
          if e.target.id == 'start'
            @start = e.target.value
          if e.target.id == 'end'
            @end = e.target.value

render = (params) ->
  id = params.get('userId')
  viewable(id).then (flag) ->
    unless flag
      return
    fetch("/api/user/#{id}/focal?#{params.toString()}", {credentials: 'include'})
      .then (res) -> res.json()
      .then (json) ->
        chart = renderFocalGraph(json)
        clickFocal(chart)
    fetch("/api/user/#{id}/iso?#{params.toString()}", {credentials: 'include'})
      .then (res) -> res.json()
      .then (json) ->
        chart = renderISOGraph(json)
        clickISO(chart)
    fetch("/api/user/#{id}/fNumber?#{params.toString()}", {credentials: 'include'})
      .then (res) -> res.json()
      .then (json) ->
        chart = renderFNumberGraph(json)
        clickFNumber(chart)
    fetch("/api/user/#{id}/exposure?#{params.toString()}", {credentials: 'include'})
      .then (res) -> res.json()
      .then (json) ->
        chart = renderExposureGraph(json)
        clickExposure(chart)
    fetch("/api/user/#{id}/camera?#{params.toString()}", {credentials: 'include'})
      .then (res) -> res.json()
      .then (json) ->
        chart = renderCameraGraph(json)
        clickCamera(chart)
    fetch("/api/user/#{id}/lens?#{params.toString()}", {credentials: 'include'})
      .then (res) -> res.json()
      .then (json) ->
        chart = renderLensGraph(json)
        clickLens(chart)

viewable = (userId) ->
  fetch("/api/user/#{userId}/viewable", {credentials: 'include'})
    .then (res) -> res.text()
    .then (text) -> text == 'true'

renderFilters = (params) ->
  camera = if params.get('camera')
    fetch("/api/camera/#{params.get('camera')}")
      .then (res) -> res.json()
  else Promise.resolve([null])
  lens = if params.get('lens')
    fetch("/api/lens/#{params.get('lens')}")
      .then (res) -> res.json()
  else Promise.resolve([null])
  Promise.all([camera, lens]).then (values) ->
    filters = {
      focal: params.get('focal'),
      iso: params.get('iso'),
      fNumber: params.get('fNumber'),
      exposure: params.get('exposure'),
      camera: values[0]?.name,
      lens: values[1]?.name
    }
    new Vue
      el: '#filters'
      data:
        filters: _.pickBy filters, _.identity
      methods:
        remove: (param) =>
          params.delete(param)
          location.search = params.toString()

focalGroup = [10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 150, 200, 250, 300, 400, 500, 600, 800, 1000, 1500, 2000]
renderFocalGraph = (elems) ->
  ctx = getCtx('focalChart')
  _elems = _.chain(focalGroup).zip(_.tail(focalGroup))
    .map (xs) ->
      targets = _.filter elems, (elem) -> xs[0] <= elem['focal'] && elem['focal'] < xs[1]
      count = _.sumBy targets, (x) -> x['count']
      {focal: xs[0], count: count}
    .dropWhile (x) -> x['count'] == 0
    .dropRightWhile (x) -> x['count'] == 0
    .value()
  labels = _elems.map (elem) -> elem['focal']
  values = _elems.map (elem) -> elem['count']
  color = labels.map (focal) ->
    v = Math.round(gradient(Math.log2(20), Math.log2(600), 192, 0, Math.log2(focal)))
    "rgba(#{v}, #{v}, #{v}, 0.8)"
  new Chart ctx, {
    type: 'bar'
    data:
      labels: labels
      datasets: [{
        label: 'image counts'
        data: values
        backgroundColor: color
      }]
    options: _.merge(barOptions('Focal count (35mm equivalent)'), xScaleLabel('mm'))
  }

clickFocal = (chart) ->
  document.getElementById('focalChart').onclick = (evt) ->
    points = chart.getElementsAtEvent(evt)
    focal = points[0]._model.label
    focalMax = (_.dropWhile focalGroup, (x) -> x <= focal)[0] || 10000000
    addParams({focal: "#{focal}_#{focalMax}"})

renderISOGraph = (elems) ->
  ctx = getCtx('isoChart')
  elemGroup = _.groupBy elems, (elem) ->
    Math.floor(Math.log2(elem['iso'] / 100))
  elems = _.map elemGroup, (xs) ->
    {iso: xs[0]['iso'], count: _.sumBy xs, (x) -> x['count']}
  labels = elems.map (elem) -> elem['iso']
  values = elems.map (elem) -> elem['count']
  color = labels.map (iso) ->
    v = Math.round(gradient(2, Math.log2(64), 192, 0, Math.log2(iso / 100)))
    "rgba(#{v}, #{v}, #{v}, 0.8)"
  new Chart ctx, {
    type: 'bar'
    data:
      labels: labels
      datasets: [{
        label: 'image counts'
        data: values
        backgroundColor: color
      }]
    options: barOptions('ISO count')
  }

clickISO = (chart) ->
  document.getElementById('isoChart').onclick = (evt) ->
    points = chart.getElementsAtEvent(evt)
    iso = points[0]._model.label
    isoMax = iso * 2
    addParams({iso: "#{iso}_#{isoMax}"})

fNumberGroup = [0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 25, 30]
renderFNumberGraph = (elems) ->
  ctx = getCtx('fNumberChart')
  _elems = _.chain(fNumberGroup).zip(_.tail(fNumberGroup))
    .map (xs) ->
      targets = _.filter elems, (elem) -> xs[0] <= elem['fNumber'] && elem['fNumber'] < xs[1]
      count = _.sumBy targets, (x) -> x['count']
      {fNumber: xs[0], count: count}
    .dropWhile (x) -> x['count'] == 0
    .dropRightWhile (x) -> x['count'] == 0
    .value()
  labels = _elems.map (elem) -> elem['fNumber']
  values = _elems.map (elem) -> elem['count']
  color = labels.map (fNumber) ->
    v = Math.round(gradient(Math.log2(1), Math.log2(10), 192, 0, Math.log2(fNumber)))
    "rgba(#{v}, #{v}, #{v}, 0.8)"
  new Chart ctx, {
    type: 'bar'
    data:
      labels: labels
      datasets: [{
        label: 'image counts'
        data: values
        backgroundColor: color
      }]
    options: barOptions('F-Number count')
  }

clickFNumber = (chart) ->
  document.getElementById('fNumberChart').onclick = (evt) ->
    points = chart.getElementsAtEvent(evt)
    fNumber = points[0]._model.label
    fNumberMax = (_.dropWhile fNumberGroup, (x) -> x <= fNumber)[0] || 10000000
    addParams({fNumber: "{fNumber}_#{fNumberMax}"})


exposureGroup = [-30, -15, -8, -4, -2, 1, 2, 4, 8, 15, 30, 60, 125, 250, 500, 1000, 2000, 4000, 8000, 16000].reverse()
renderExposureGraph = (elems) ->
  ctx = getCtx('exposureChart')
  elemGroup = _.groupBy elems, (elem) ->
    _.find exposureGroup, (x) -> x <= elem['exposure']
  elems = _.map elemGroup, (xs) ->
    {exposure: xs[0]['exposure'], count: _.sumBy xs, (x) -> x['count']}
  labels = elems.map (elem) -> elem['exposure']
  values = elems.map (elem) -> elem['count']
  color = labels.map (exposure) ->
    v = Math.round(gradient(1, Math.sqrt(4000), 0, 192, Math.sqrt(exposure)))
    "rgba(#{v}, #{v}, #{v}, 0.8)"
  new Chart ctx, {
    type: 'bar'
    data:
      labels: labels
      datasets: [{
        label: 'image counts'
        data: values
        backgroundColor: color
      }]
    options: barOptions('Shutter speed count')
  }

clickExposure = (chart) ->
  document.getElementById('exposureChart').onclick = (evt) ->
    points = chart.getElementsAtEvent(evt)
    exposure = points[0]._model.label
    exposureMax = (_.dropWhile exposureGroup, (x) -> exposure <= x)[0] || -99999
    addParams({exposure: "#{exposure}_#{exposureMax}"})

cameraIds = []
renderCameraGraph = (elems) ->
  ctx = getCtx('cameraChart')
  values = elems.map (elem) -> elem['count']
  labels = elems.map (elem) -> elem['camera']
  cameraIds = elems.map (elem) -> elem['id']
  new Chart ctx, {
    type: 'pie'
    data:
      labels: labels
      datasets: [{
        data: values
        backgroundColor: colorSet(0.5)
      }]
  }

clickCamera = (chart) ->
  document.getElementById('cameraChart').onclick = (evt) ->
    points = chart.getElementsAtEvent(evt)
    camera = cameraIds[points[0]._index]
    addParams({camera: camera})

lensIds = []
renderLensGraph = (elems) ->
  ctx = getCtx('lensChart')
  values = elems.map (elem) -> elem['count']
  labels = elems.map (elem) -> elem['lens']
  lensIds = elems.map (elem) -> elem['id']
  new Chart ctx, {
    type: 'pie'
    data:
      labels: labels
      datasets: [{
        data: values
        backgroundColor: colorSet(0.5)
      }]
  }

clickLens = (chart) ->
  document.getElementById('lensChart').onclick = (evt) ->
    points = chart.getElementsAtEvent(evt)
    lens = lensIds[points[0]._index]
    addParams({lens: lens})

colorSet = (a) -> [
  "rgba(0, 65, 255, #{a})",
  "rgba(255, 40, 0, #{a})",
  "rgba(53, 161, 107, #{a})",
  "rgba(250, 245, 0, #{a})",
  "rgba(102, 204, 255, #{a})"
]

getCtx = (id) -> document.getElementById(id).getContext('2d')

gradient = (min, max, minValue, maxValue, input) ->
  v = Math.max(min, Math.min(max, input))
  rate = (v - min)/(max - min)
  (maxValue - minValue) * rate + minValue

barOptions = (title) ->
  legend:
    display: false
  title:
    display: true
    text: title
    fontSize: 18
  scales:
    yAxes: [{
      ticks: {
        beginAtZero: true
      }
    }]

logarithmicOptions = ->
  scales:
    xAxes: [{
      type: 'logarithmic',
      ticks:
        callback: Chart.Ticks.formatters.linear
    }]

xScaleLabel = (label) ->
  scales:
    xAxes: [{
      scaleLabel:
        display: true
        labelString: label
    }]

addParams = (params) ->
  p = new URLSearchParams(location.search.slice(1))
  for k, v of params
    if v
      if p.has(k)
        p.delete(k)
      p.append(k, v)
  location.search = p.toString()
