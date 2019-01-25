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

gulp.task 'copyjs', ->
  gulp.src './lib/js/**/*.js'
    .pipe plumber()
    .pipe gulp.dest('./output/lib/')

gulp.task 'compile', gulp.series(gulp.parallel('pug', 'coffee', 'copy', 'copyjs'))

gulp.task 'watch', gulp.series 'compile', ->
  gulp.watch('./src/pug/**/*.pug', gulp.task 'pug')
  gulp.watch('./src/coffee/**/*.coffee', gulp.task 'coffee')
  gulp.watch('./src/js/**/*.js', gulp.task 'copyjs')

gulp.task 'default', gulp.series('watch')
