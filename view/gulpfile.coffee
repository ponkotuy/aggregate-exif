gulp = require 'gulp'
jade = require 'gulp-pug'
coffee = require 'gulp-coffee'
plumber = require 'gulp-plumber'

gulp.task 'pug', ->
  gulp.src ['./src/pug/**/*.pug', '!./src/pug/**/_*.pug']
    .pipe plumber()
    .pipe jade({pretty: true})
    .pipe gulp.dest('./output/')

gulp.task 'coffee', ->
  gulp.src './src/coffee/**/*.coffee'
    .pipe plumber()
    .pipe coffee()
    .pipe gulp.dest('./output/js/')

gulp.task 'copy', ->
  gulp.src './node_modules/bootstrap-material-design/dist/**'
    .pipe plumber()
    .pipe gulp.dest('./output/lib/bootstrap-material-design/')

gulp.task 'compile', ['pug', 'coffee', 'copy']

gulp.task 'watch', ['compile'], ->
  gulp.watch('./src/pug/**/*.pug', ['pug'])
  gulp.watch('./src/coffee/**/*.coffee', ['coffee'])

gulp.task 'default', ['watch']
