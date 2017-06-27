$(document).ready ->
  params = fromURLParameter(location.search.slice(1))
  id = params.userId
  fetch("/api/user/#{id}/iso")
    .then (res) -> res.json()
    .then (json) -> renderGraph(json)
  fetch("/api/user/#{id}/focal")
    .then (res) -> res.json()
    .then (json) -> renderFocalGraph(json)

renderGraph = (elems) ->
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

renderFocalGraph = (elems) ->
  ctx = getCtx('focalChart')
  data = elems.map (elem) -> {x: elem['focal'], y: elem['count']}
  labels = elems.map (elem) -> elem['focal']
  logarithmicOptions =
    scales:
      xAxes: [{
        type: 'logarithmic',
        ticks:
          callback: Chart.Ticks.formatters.linear
      }]
  new Chart ctx, {
    type: 'line'
    data:
      labels: labels
      datasets: [{
        label: 'image counts'
        data: data
      }]
    options: _.merge(barOptions('Focal count (35mm equivalent)'), logarithmicOptions)
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
