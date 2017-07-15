/*
 *TESTED(24/01/2013): FireFox, GoogleChrome, IE10, Opera
 *Minify a jpeg image without loosing EXIF
 *
 *To minify jpeg image:
 *target image must be loaded into Image instance before minifying
 *    var img = new Image();
 *    img.onload = function(){
 *        minified = MinifyJpeg.minify(image, length);
 *        enc = "data:image/jpeg;base64," + MinifyJpeg.encode64(minified);
 *        html = '<img src="' + enc + '">';
 *    }
 *    img.src = image
 *
 *MinifyJpeg.minify() - return Uint8Array
 *image - image base64encoded, it can be obtained "FileReader().readAsDataURL(f)"
 *length - the long side length of the rectangle
 *MinifyJpeg.encode64() - convert array to base64encoded string
 */

MinifyJpeg = {};

MinifyJpeg.minify = function(imageStr, chouhen)
{
    this.NEW_SIZE = parseInt(chouhen);
    this.rawImage = this.decode64(imageStr.replace("data:image/jpeg;base64,", ""));
    this.segments = this.slice2Segments(this.rawImage);
    this.resizeImage(imageStr);

    if (this.boolLostExif)
    {
        this.image = this.exifManipulation(this.resizedImage, this.rawImage);
    }
    else
    {
        this.image = this.array2ArrayBuffer(this.rawImage);
    }
    return this.image;
};

MinifyJpeg.keyStr = "ABCDEFGHIJKLMNOP" +
                    "QRSTUVWXYZabcdef" +
                    "ghijklmnopqrstuv" +
                    "wxyz0123456789+/" +
                    "=";

MinifyJpeg.SOF = [192, 193, 194, 195, 197, 198, 199, 201, 202, 203, 205, 206, 207];

MinifyJpeg.getImageSize = function(imageArray)
{
    var segments = this.slice2Segments(imageArray);
    return this.imageSizeFromSegments(segments);
};

MinifyJpeg.slice2Segments = function(rawImage)
{
    var head = 0,
        segments = [];
    while (1)
    {
        if (rawImage[head] === 255 & rawImage[head + 1] === 218){break;}
        if (rawImage[head] === 255 & rawImage[head + 1] === 216)
        {
            head += 2;
        }
        else
        {
            var length = rawImage[head + 2] * 256 + rawImage[head + 3],
                endPoint = head + length + 2,
                seg = rawImage.slice(head, endPoint);
            segments.push(seg);
            head = endPoint;
        }
        if (head > rawImage.length){break;}
    }
    return segments;
};

MinifyJpeg.imageSizeFromSegments = function(segments)
{
    for  (var x=0; x<segments.length; x++)
    {
        var seg = segments[x];
        if (this.SOF.indexOf(seg[1]) >= 0)
        {
            var height = seg[5] * 256 + seg[6],
                width = seg[7] * 256 + seg[8];
            break;
        }
    }
    return [width, height];
};

MinifyJpeg.encode64 = function(input)
{
    var output = "",
        chr1, chr2, chr3 = "",
        enc1, enc2, enc3, enc4 = "",
        i = 0;

    do {
        chr1 = input[i++];
        chr2 = input[i++];
        chr3 = input[i++];

        enc1 = chr1 >> 2;
        enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
        enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
        enc4 = chr3 & 63;

        if (isNaN(chr2)) {
           enc3 = enc4 = 64;
        } else if (isNaN(chr3)) {
           enc4 = 64;
        }

        output = output +
           this.keyStr.charAt(enc1) +
           this.keyStr.charAt(enc2) +
           this.keyStr.charAt(enc3) +
           this.keyStr.charAt(enc4);
        chr1 = chr2 = chr3 = "";
        enc1 = enc2 = enc3 = enc4 = "";
    } while (i < input.length);

    return output;
};

MinifyJpeg.decode64 = function(input) {
    var output = "",
        chr1, chr2, chr3 = "",
        enc1, enc2, enc3, enc4 = "",
        i = 0,
        buf = [];

    // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
    var base64test = /[^A-Za-z0-9\+\/\=]/g;
    if (base64test.exec(input)) {
        alert("There were invalid base64 characters in the input text.\n" +
              "Valid base64 characters are A-Z, a-z, 0-9, '+', '/',and '='\n" +
              "Expect errors in decoding.");
    }
    input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

    do {
        enc1 = this.keyStr.indexOf(input.charAt(i++));
        enc2 = this.keyStr.indexOf(input.charAt(i++));
        enc3 = this.keyStr.indexOf(input.charAt(i++));
        enc4 = this.keyStr.indexOf(input.charAt(i++));

        chr1 = (enc1 << 2) | (enc2 >> 4);
        chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
        chr3 = ((enc3 & 3) << 6) | enc4;

        buf.push(chr1);

        if (enc3 !== 64) {
           buf.push(chr2);
        }
        if (enc4 !== 64) {
           buf.push(chr3);
        }

        chr1 = chr2 = chr3 = "";
        enc1 = enc2 = enc3 = enc4 = "";

    } while (i < input.length);

    return buf;
};

MinifyJpeg.resizeImage = function(imageStr)
{
    var img0 = new Image();
    img0.src = imageStr;

    var width = img0.width,
        height = img0.height,
        chouhen = (width>=height) ? width : height,
        newSize = this.NEW_SIZE,  //px
        canvas0 = document.createElement('canvas');

    if (chouhen < newSize)
    {
        this.boolLostExif = false;
        return;
    }
    else{
        var canvasWidth = parseFloat(newSize)/chouhen * width;
        var canvasHeight = parseFloat(newSize)/chouhen * height;
    }
    canvas0.width = parseInt(canvasWidth);
    canvas0.height = parseInt(canvasHeight);

    var context = canvas0.getContext("2d");
    context.drawImage(img0, 0, 0, canvasWidth, canvasHeight);
    var resizedImage = canvas0.toDataURL("image/jpeg");

    this.boolLostExif = true;
    this.resizedImage = resizedImage;
    return;
};

MinifyJpeg.getExifArray = function(segments)
{
    var seg;
    for (var x=0; x<segments.length; x++)
    {
        seg = segments[x];
        if (seg[0] === 255 & seg[1] === 225) //(ff e1)
        {
            return seg;
        }
    }
    return false;
};

MinifyJpeg.insertExif = function(imageStr, exif)
{
    var imageData = imageStr.replace("data:image/jpeg;base64,", ""),
        buf = this.decode64(imageData),
        separatePoint = buf.indexOf(255,3),
        mae = buf.slice(0, separatePoint),
        ato = buf.slice(separatePoint),
        array = mae;

    for (var x=0; x<exif.length; x++)
    {
        array.push(exif[x]);
    }
    array = array.concat(ato);
    return array
};

MinifyJpeg.exifManipulation = function(lostExifImage, rawImage)
{
    var exif = this.getExifArray(this.segments),
        newArray = this.insertExif(lostExifImage, exif),
        bArray = this.array2ArrayBuffer(newArray);

    return bArray;
};

MinifyJpeg.array2ArrayBuffer = function(array)
{
    var bArray = new Uint8Array(array.length);
    for (var x=0; x<array.length; x++)
    {
        bArray[x] = array[x];
    }
    return bArray;
};
