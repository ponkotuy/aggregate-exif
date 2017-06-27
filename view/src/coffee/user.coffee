$(document).ready ->
  params = fromURLParameter(location.search.slice(1))
  id = params.userId
  fetch("/api/user/#{id}/focal")
    .then (res) -> res.json()
    .then (json) -> renderFocalGraph(json)
  fetch("/api/user/#{id}/iso")
    .then (res) -> res.json()
    .then (json) -> renderISOGraph(json)
  fetch("/api/user/#{id}/f_number")
    .then (res) -> res.json()
    .then (json) -> renderFNumberGraph(json)
  fetch("/api/user/#{id}/exposure")
    .then (res) -> res.json()
    .then (json) -> renderExposureGraph(json)

renderFocalGraph = (elems) ->
  ctx = getCtx('focalChart')
  data = elems.map (elem) -> {x: elem['focal'], y: elem['count']}
  new Chart ctx, {
    type: 'line'
    data:
      datasets: [{
        label: 'image counts'
        data: data
      }]
    options: _.merge(barOptions('Focal count (35mm equivalent)'), logarithmicOptions(), xScaleLabel('mm'))
  }

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

renderFNumberGraph = (elems) ->
  ctx = getCtx('fNumberChart')
  data = elems.map (elem) -> {x: elem['fNumber'], y: elem['count']}
  labels = elems.map (elem) -> elem['fNumber']
  new Chart ctx, {
    type: 'scatter'
    data:
      labels: labels
      datasets: [{
        label: 'image counts'
        data: data
      }]
    options: _.merge(barOptions('F-Number count'), logarithmicOptions())
  }

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
