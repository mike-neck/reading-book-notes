#!/usr/bin/env groovy

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

def original = Paths.get('.').toAbsolutePath().normalize()
def currentDir = original

while (!currentDir.endsWith('reading-book-notes')) {
    currentDir = currentDir.parent
    if (currentDir == null) throw new IllegalStateException("${original} is not project directroy.")
}

def docDir = currentDir.resolve('src').resolve('main').resolve('doc')

class Visitor implements FileVisitor<Path> {

    final Path root

    Visitor(Path root) {
        this.root = root
    }

    List<Path> files = []

    String getPagesGroovy() {
        files.collect { "\"$it\"" }.join('\n')
    }

    @Override
    FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE
    }

    @Override
    FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        files << root.relativize(file)
        return FileVisitResult.CONTINUE
    }

    @Override
    FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        if (exc != null) throw exc
        return FileVisitResult.CONTINUE
    }

    @Override
    FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
        if (exc != null) throw exc
        return FileVisitResult.CONTINUE
    }
}

def visitor = new Visitor(docDir)

Files.walkFileTree(docDir, visitor)

def pagesGroovy = Paths.get('pages.groovy')

pagesGroovy.toFile().write(visitor.pagesGroovy, 'UTF-8')
