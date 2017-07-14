$(document).ready ->
  document.getElementById('uploadTab').className = 'active'
  mustSession()
  $('#exif').click ->
    files = $('#inputFile').prop('files')
    console.log(files)
    for file in files
      parseEXIF(file)

parseEXIF = (file) ->
  EXIF.getData file, ->
    tags = EXIF.getAllTags(@)
    blob = file.slice(0, 300)
    console.log(blob)
    reader = new FileReader()
    reader.onload = (res) ->
      console.log(convertBinaryStringToUint8Array(res.target.result))
    reader.readAsBinaryString(blob)
    result = 0
    while true
      result = searchArray(tags.MakerNote, [81, 0, 2, 0], result + 1)
      console.log(result)
      if result == -1
        break
      console.log(tags.MakerNote.slice(result, result + 300))
    result = {
      fileName: file.name
      cond:
        iso: tags.ISOSpeedRatings
        focal: tags.FocalLength
        focal35: tags.FocalLengthIn35mmFilm
        fNumber: tags.FNumber
        exposure: tags.ExposureTime
      dateTime: tags.DateTime
      camera:
        maker: tags.Make
        model: tags.Model
    }
    console.log(result)

searchArray = (orig, ary, idx = 0) ->
  f = (i) ->
    _.isEqual(orig.slice(i, i + ary.length), ary)
  _.findIndex [0...(orig.length - ary.length)], f, idx


convertBinaryStringToUint8Array = (bStr) ->
  len = bStr.length
  u8_array = new Uint8Array(len)
  [0...len].forEach (i) ->
    u8_array[i] = bStr.charCodeAt(i)
  u8_array
