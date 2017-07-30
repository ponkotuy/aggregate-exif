$(document).ready ->
  document.getElementById('uploadTab').className = 'active'
  mustSession()
  results = renderResults()
  uploader = document.getElementById('inputFiles')
  document.getElementById('exif').onclick = ->
    for file in uploader.files
      imageMinimize file, (form, file_) ->
        send(form).then (res) ->
          res.text().then (text) ->
            results.push {success: res.ok, text: text, name: file_.name}
  fetch('/api/images/count', {credentials: 'include'})
    .then (res) -> res.json()
    .then (json) -> renderImages(json)

imageMinimize = (file, f) ->
  reader = new FileReader()
  reader.onload = (e) ->
    data = e.target.result
    minified = MinifyJpeg.minify(data.replace(/\r?\n/g, ''), 1)
    blob = new Blob([minified], {type: 'image/jpeg'})
    form = new FormData()
    form.append('file', blob, file.name)
    f(form, file)
  reader.readAsDataURL(file)

send = (form) ->
  fetch '/api/image',
    method: 'POST'
    body: form
    credentials: 'include'

renderResults = ->
  new Vue
    el: '#results'
    data:
      results: []
    methods:
      push: (r) ->
        @results.push(r)

renderImages = (pageCount) ->
  new Vue
    el: '#images'
    data:
      page: 0
      images: []
      pageCount: Math.min(pageCount.page, 10)
    methods:
      getImages: (page) ->
        fetch("/api/images?page=#{page}", {credentials: 'include'})
          .then (res) -> res.json()
          .then (json) =>
            @images = json
      nextPage: (page) ->
        @page = Math.max(0, Math.min(@pageCount - 1, page))
        @getImages(@page)
    mounted: ->
      @getImages(0)
