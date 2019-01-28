$(document).ready ->
  Vue.use(VueTables.ServerTable, theme = 'bootstrap3')
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
  renderImages()

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

renderImages = ->
  new Vue
    el: '#images'
    data:
      columns: ['checkbox', 'fileName', 'dateTime', 'createdAt']
      checkedRows: []
      allChecked: false
      options:
        sortable: ['fileName', 'dateTime', 'createdAt']
        orderBy: {ascending: false, column: 'dateTime'}
        perPage: 100
        headings:
          checkbox: (h) -> createCheckbox(h, @)
          fileName: 'Name'
          dateTime: 'ShootingTime'
          createdAt: 'UploadingTime'
    methods:
      deleteImages: ->
        json = JSON.stringify(@checkedRows.map (x) -> x.id)
        fetch('/api/images', {body: json, method: 'DELETE', headers: {'Content-Type': 'application/json'}, credentials: 'include'})
          .then -> location.reload(false)

createCheckbox = (h, self) ->
  h 'input',
    attrs:
      type: 'checkbox'
    on:
      click: ->
        self.allChecked = !self.allChecked
        $('.image-checkbox').click()
    ref: 'selectAllCheckbox'
