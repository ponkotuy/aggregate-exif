$(document).ready ->
  document.getElementById('uploadTab').className = 'active'
  mustSession()
  uploader = document.getElementById('inputFiles')
  document.getElementById('exif').onclick = ->
    for file in uploader.files
      imageMinimize(file, send)

imageMinimize = (file, f) ->
  reader = new FileReader()
  reader.onload = (e) ->
    data = e.target.result
    img = new Image()
    img.onload = ->
      canvas = document.getElementById('tmp')
      ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, 1, 1)
      src = ctx.canvas.toDataURL('image/jpeg')
      blob = dataURLtoBlob(src)
      form = new FormData()
      form.append('file', blob, file.name)
      f(form)
    img.src = data
  reader.readAsDataURL(file)

dataURLtoBlob = (url) ->
  bin = atob(url.split('base64,')[1])
  barr = new Uint8Array(bin.length)
  for i in [0...bin.length]
    barr[i] = bin.charCodeAt(i)
  new Blob([barr], {type: 'image/jpeg'})

send = (form) ->
  fetch '/api/image',
    method: 'POST'
    body: form
    credentials: 'include'
