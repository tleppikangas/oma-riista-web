"use strict";

angular.module('app.harvestpermit', [
    'app.harvestpermit.search.permit',
    'app.harvestpermit.search.application',
    'app.harvestpermit.services',
    'app.harvestpermit.decision',
    'app.harvestpermit.decision.application',
    'app.harvestpermit.decision.area',
    'app.harvestpermit.decision.actions',
    'app.harvestpermit.decision.species',
    'app.harvestpermit.decision.attachments',
    'app.harvestpermit.decision.document',
    'app.harvestpermit.decision.revisions',
    'app.harvestpermit.decision.rkarecipient',
    'app.harvestpermit.decision.rkaauthority',
    'app.harvestpermit.application',
    'app.harvestpermit.application.area',
    'app.harvestpermit.application.conflict',
    'app.harvestpermit.application.fragment',
    'app.harvestpermit.application.wizard',
    'app.harvestpermit.management',
    'app.harvestpermit.management.payment',
    'app.harvestpermit.management.allocation',
    'app.harvestpermit.management.usage',
    'app.harvestpermit.management.contactpersons',
    'app.harvestpermit.management.endofhunting',
    'app.harvestpermit.management.tables',
    'app.moosepermit.controllers',
    'app.moosepermit.services',
    'app.moosepermit.directives',
    'app.moosepermit.map',
    'app.moosepermit.luke',
    'app.moosepermit.moosehuntingsummary',
    'app.moosepermit.deerhuntingsummary'
]);