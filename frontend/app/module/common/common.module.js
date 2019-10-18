"use strict";

angular.module('app.common', [
    'app.common.validation',
    'app.common.directives',
    'app.common.services',
    'app.common.filters',
    'app.common.download',
    'app.common.pagination.slice',
    'app.common.huntingyear',
    'app.common.species',
    'app.common.personsearch',
    'app.common.specimen',
    'app.common.askreason',
    'app.common.map.bounds',
    'app.common.map.services',
    'app.common.map.directives',
    'app.mapeditor.core',
    'app.mapeditor.ui',
    'app.mapeditor.selection',
    'app.mapeditor.draw',
    'app.mapeditor.mml',
    'app.mapeditor.metsahallitus',
    'app.mapeditor.excluded',
    'app.mapeditor.sidebar'
]);
