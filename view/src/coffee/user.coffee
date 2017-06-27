$(document).ready ->
  params = fromURLParameter(location.search.slice(1))
  fetch("/api/user/#{params.userId}/iso")
    .then (res) -> res.json()
    .then (json) -> renderGraph(json)

renderGraph = (elems) ->
  ctx = document.getElementById('isoChart').getContext('2d')
  elemGroup = _.groupBy elems, (elem) ->
    Math.floor(Math.log2(elem['iso'] / 100))
  elems = _.map elemGroup, (xs) ->
    {iso: xs[0]['iso'], count: _.sumBy xs, (x) -> x['count']}
  labels = elems.map (elem) -> elem['iso']
  values = elems.map (elem) -> elem['count']
  color = labels.map (iso) ->
    v = Math.round(gradient(2, Math.log2(64), 128, 0, Math.log2(iso / 100)))
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
    options:
      title:
        display: true
        text: ''
  }

gradient = (min, max, minValue, maxValue, input) ->
  v = Math.max(min, Math.min(max, input))
  rate = (v - min)/(max - min)
  (maxValue - minValue) * rate + minValue
