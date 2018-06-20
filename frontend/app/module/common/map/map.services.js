'use strict';

angular.module('app.common.map.services', [])
    .service('MapUtil', function () {
        this.isValidLatLng = function (latlng) {
            return _.isObject(latlng) &&
                isFinite(latlng.lat) &&
                isFinite(latlng.lng) &&
                latlng.lat > 0 &&
                latlng.lng > 0;
        };

        this.isValidGeoLocation = function (geoLocation) {
            return _.isObject(geoLocation) &&
                isFinite(geoLocation.latitude) &&
                isFinite(geoLocation.longitude) &&
                geoLocation.latitude > 0 &&
                geoLocation.longitude > 0;
        };

        this.limitDefaultZoom = function (currentZoom) {
            var defaultZoom = 11;
            var minZoom = 7;
            var maxZoom = 16;

            if (!isFinite(currentZoom)) {
                return defaultZoom;
            }

            if (currentZoom < minZoom) {
                return minZoom;
            }

            if (currentZoom > maxZoom) {
                return maxZoom;
            }

            return currentZoom;
        };

        this.getDefaultGeoLocation = function () {
            return {
                latitude: 7150000,
                longitude: 570000,
                zoom: 6
            };
        };
    })
    .service('MapState', function (WGS84, MapUtil, LocalStorageService) {
        var state = {
            layerName: LocalStorageService.getKey('selectedLayerName'),
            overlayNames: loadOverlayNames(),
            center: {},
            viewBounds: null
        };

        this.reset = function () {
            state.center = {};
            state.viewBounds = null;
        };

        this.get = function () {
            return state;
        };

        this.getSelectedLayerName = function () {
            return state.layerName;
        };

        this.getSelectedOverlayNames = function () {
            return state.overlayNames;
        };

        this.setSelectedLayerName = function (name) {
            state.layerName = name;
            LocalStorageService.setKey('selectedLayerName', name);
        };

        this.setOverlayEnabled = function (overlayName) {
            state.overlayNames = state.overlayNames || [];
            state.overlayNames = _.pull(state.overlayNames, overlayName);
            state.overlayNames.push(overlayName);
            storeOverlayNames();
        };

        this.setOverlayDisabled = function (overlayName) {
            state.overlayNames = state.overlayNames || [];
            state.overlayNames = _.pull(state.overlayNames, overlayName);
            storeOverlayNames();
        };

        function storeOverlayNames() {
            LocalStorageService.setKey('selectedOverlayNames', JSON.stringify(state.overlayNames));
        }

        function loadOverlayNames() {
            try {
                var value = JSON.parse(LocalStorageService.getKey('selectedOverlayNames'));
                return _.isArray(value) ? value : null;
            } catch (e) {
                return null;
            }
        }

        this.getZoom = function () {
            return state.center ? state.center.zoom : null;
        };

        this.setMapCenterLatLng = function (latlng) {
            if (MapUtil.isValidLatLng(latlng)) {
                state.center = angular.copy(latlng);
            }
        };

        this.updateMapCenter = function (geoLocation, zoom) {
            var latlng = WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude);

            if (angular.isObject(state.center)) {
                state.center.lat = latlng.lat;
                state.center.lng = latlng.lng;
            } else {
                state.center = angular.copy(latlng);
            }

            state.center.zoom = MapUtil.limitDefaultZoom(zoom || state.center.zoom);
        };

        this.updateMapBounds = function (bounds, defaultBounds, forceUpdate) {
            if (state.viewBounds === null || state.center === null) {
                forceUpdate = true;
            }

            if (forceUpdate) {
                if (bounds) {
                    state.viewBounds = bounds;
                } else if (defaultBounds) {
                    state.viewBounds = defaultBounds;
                }

                if (state.center) {
                    state.center = {};
                }
            }
        };

        this.toGeoLocationOrDefault = function (defaultGeoLocation) {
            if (MapUtil.isValidLatLng(state.center)) {
                var c = WGS84.toETRS(state.center.lat, state.center.lng);

                return {
                    latitude: c.lat,
                    longitude: c.lng,
                    zoom: state.center.zoom
                };
            }

            // fallback to default
            if (MapUtil.isValidGeoLocation(defaultGeoLocation)) {
                return defaultGeoLocation;
            }

            return MapUtil.getDefaultGeoLocation();
        };
    })

    .service('WGS84', function () {
        function toLatLng(xy) {
            return {lat: xy.y, lng: xy.x};
        }

        function convert(from, to, lat, lng) {
            return toLatLng(proj4(from, to, {x: lng, y: lat}));
        }

        function round(latlng) {
            return {
                lat: Math.round(latlng.lat),
                lng: Math.round(latlng.lng)
            };
        }

        this.toETRS = function (lat, lng) {
            return round(convert('EPSG:4326', 'EPSG:3067', lat, lng));
        };

        this.fromETRS = function (lat, lng) {
            return convert('EPSG:3067', 'EPSG:4326', lat, lng);
        };

        this.fromKKJ = function (lat, lng) {
            return convert('EPSG:2393', 'EPSG:4326', lat, lng);
        };

        this.toKKJ = function (lat, lng) {
            return convert('EPSG:4326', 'EPSG:2393', lat, lng);
        };
    })

    .service('MapLayers', function (WGS84, MapState, $translate) {
        var maxBounds = {
            southWest: WGS84.fromETRS(6291456, -548576),
            northEast: WGS84.fromETRS(8388608, 1548576)
        };

        var mmlUrlTemplate = _.template('https://kartta.riista.fi/tms/1.0.0/<%= layer %>/EPSG_3857/{z}/{x}/{y}.png');
        var mmlUrls = {
            terrain: mmlUrlTemplate({layer: 'maasto_mobile'}),
            background: mmlUrlTemplate({layer: 'tausta_mobile'}),
            aerial: mmlUrlTemplate({layer: 'orto_mobile'}),
            empty: ''
        };

        this.createLayer = function (type) {
            return {
                name: $translate.instant('global.map.layer.' + type),
                url: mmlUrls[type],
                type: 'xyz',
                layerOptions: {
                    type: 'xyz',
                    format: 'image/png',
                    minZoom: 0,
                    maxZoom: 16,
                    maxNativeZoom: 16,
                    detectRetina: false,
                    continuousWorld: true,
                    worldCopyJump: false,
                    tms: true,
                    bounds: L.latLngBounds(maxBounds.southWest, maxBounds.northEast),
                    attribution: '&copy;<a href="http://www.maanmittauslaitos.fi" target="_blank">Maanmittauslaitos</a>'
                }
            };
        };

        var vectorLayerTemplate = _.template('https://kartta.riista.fi/vector/<%= layer %>/{z}/{x}/{y}');

        this.createVectorLayer = function (layerName, style) {
            var vectorTileLayerStyles = {};
            vectorTileLayerStyles[layerName] = style;

            return L.vectorGrid.protobuf(vectorLayerTemplate({layer: layerName}), {
                pane: 'overlayPane',
                updateWhenZooming: true,
                keepBuffer: 10,
                maxZoom: 16,
                rendererFactory: L.canvas.tile,
                bounds: L.latLngBounds(maxBounds.southWest, maxBounds.northEast),
                vectorTileLayerStyles: vectorTileLayerStyles
            });
        };

        this.activateSelectedBaseLayer = function (layers) {
            var selectedLayerName = MapState.getSelectedLayerName();
            var selectedLayer = _.find(layers, function (layer) {
                return layer.name === selectedLayerName;
            });

            _.each(layers, function (layer) {
                layer.top = false;
            });

            if (selectedLayer) {
                selectedLayer.top = true;
            }
        };

        this.activateSelectedOverlays = function (layers) {
            var selectedOverlayNames = MapState.getSelectedOverlayNames();

            _.each(layers, function (overlay) {
                overlay.visible = _.contains(selectedOverlayNames, overlay.name);
            });
        };
    })

    .service('MapDefaults', function (MapLayers) {
        var mapBaseLayers = {
            terrain: MapLayers.createLayer('terrain'),
            background: MapLayers.createLayer('background'),
            aerial: MapLayers.createLayer('aerial'),
            empty: MapLayers.createLayer('empty')
        };

        function createRandomColourFunction(kohdeCount) {
            return function (feature) {
                var kohdeId = _.get(feature, 'KOHDE_ID', 0);

                // Multiply by small prime to alternate colour for bordering polygons
                var tmp = 97 * kohdeId / kohdeCount;

                return {
                    fill: true,
                    fillColor: 'hsl(' + Math.round(tmp * 256) % 256 + ',100%,40%)',
                    fillOpacity: 0.25,
                    weight: 0.75,
                    color: 'black'
                };
            };
        }

        var mapOverlays = {
            hirvi: {
                type: 'custom',
                name: 'Metsähallitus hirvi 2018',
                layer: MapLayers.createVectorLayer('hirvi', createRandomColourFunction(348))
            },
            pienriista: {
                type: 'custom',
                name: 'Metsähallitus pienriista 2018',
                layer: MapLayers.createVectorLayer('pienriista', createRandomColourFunction(123))
            },
            valtionmaat: {
                type: 'custom',
                name: 'Valtionmaat',
                layer: MapLayers.createVectorLayer('metsahallitus', {
                    fill: true,
                    fillColor: 'blue',
                    fillOpacity: 0.25,
                    weight: 0.75,
                    color: 'black'
                })
            },
            rhy: {
                type: 'custom',
                name: 'RHY rajat',
                layer: MapLayers.createVectorLayer('rhy', {
                    fill: false,
                    weight: 5.0,
                    color: 'blue'
                })
            }
        };

        this.create = function (cfg) {
            MapLayers.activateSelectedBaseLayer(mapBaseLayers);
            MapLayers.activateSelectedOverlays(mapOverlays);

            var defaults = angular.extend({}, {
                controls: {
                    scale: {
                        visible: true,
                        position: 'bottomleft',
                        maxWidth: 200,
                        imperial: false,
                        updateWhenIdle: false
                    },
                    layers: {
                        visible: true,
                        position: 'topleft',
                        collapsed: true
                    }
                },
                mmlLayers: {
                    baselayers: mapBaseLayers,
                    overlays: mapOverlays
                },
                geojsonWatchOptions: {
                    doWatch: true,
                    isDeep: false,
                    individual: {
                        doWatch: false,
                        isDeep: false
                    }
                },
                // Disable the animation on double-click and other zooms.
                zoomAnimation: false,
                fadeAnimation: false,
                inertia: true,
                minZoom: 5,
                attributionControl: true,
                zoomsliderControl: false,
                zoomControlPosition: 'topleft',

                map: {
                    crs: L.CRS.EPSG3857
                }
            });

            if (cfg && cfg.fullscreen) {
                defaults.controls.custom = new L.Control.Fullscreen({
                    pseudoFullscreen: true,
                    position: 'topright',
                    title: {
                        'false': 'Kokoruutu',
                        'true': 'Pois kokoruudusta'
                    }
                });
            }
            return defaults;
        };

        this.getGeoJsonOptions = function (overwrites) {
            return angular.extend({}, {
                fillColor: 'green',
                weight: 1,
                opacity: 1,
                color: 'black',
                fillOpacity: 0.3
            }, overwrites || {});
        };

        this.getMapBroadcastEvents = function (mapEvents) {
            // By default, most (if not all) Leaflet map events are not needed to be propagated into Angular scope(s)
            var broadcastEvents = {
                map: {
                    logic: 'emit',
                    enable: []
                },
                marker: {
                    logic: 'emit',
                    enable: []
                },
                path: {
                    logic: 'emit',
                    enable: []
                }
            };

            if (angular.isArray(mapEvents)) {
                _.each(mapEvents, function (e) {
                    broadcastEvents.map.enable.push(e);
                });
            }

            return broadcastEvents;
        };
    })

    .service('GIS', function ($http, MapBounds, $rootScope, WGS84) {
        var self = this;

        this.getPropertyIdentifierForGeoLocation = function (geoLocation) {
            return $http.get('/api/v1/gis/kt', {
                params: {
                    latitude: geoLocation.latitude,
                    longitude: geoLocation.longitude
                }
            });
        };

        this.getRhyForGeoLocation = function (geoLocation) {
            return $http.get('/api/v1/gis/rhy', {
                params: {
                    latitude: geoLocation.latitude,
                    longitude: geoLocation.longitude
                }
            });
        };

        this.getMunicipalityForGeoLocation = function (geoLocation) {
            return $http.get('/api/v1/gis/municipality', {
                params: {
                    latitude: geoLocation.latitude,
                    longitude: geoLocation.longitude
                }
            });
        };

        this.getRhyGeom = function (officialCode) {
            return $http.get('/api/v1/gis/rhy/geom', {params: {officialCode: officialCode}});
        };

        this.getInvertedRhyGeoJSON = function (officialCode, featureId, props) {
            return self.getRhyGeom(officialCode).then(function (response) {
                return {
                    type: "FeatureCollection",
                    features: [
                        {
                            type: "Feature",
                            id: featureId,
                            properties: props,
                            geometry: {
                                type: "MultiPolygon",
                                coordinates: [[[[-180, -180], [180, 0], [180, 180], [0, 180], [-180, -180]],
                                    response.data.coordinates[0][0]]]
                            }
                        }
                    ]
                };
            });
        };

        this.getRhyCenter = function (officialCode) {
            return MapBounds.getRhyBounds(officialCode).then(function (rhyBounds) {
                var center = {zoom: 8}; // hand-wavy zoom level default to show most of rhy area
                var sw = WGS84.toETRS(rhyBounds.southWest.lat, rhyBounds.southWest.lng);
                var ne = WGS84.toETRS(rhyBounds.northEast.lat, rhyBounds.northEast.lng);
                center.latitude = sw.lat + (ne.lat - sw.lat) / 2;
                center.longitude = sw.lng + (ne.lng - sw.lng) / 2;
                return center;
            });
        };

        this.getRhyMembershipBoundsOrNull = function () {
            if ($rootScope.account && $rootScope.account.rhyMembership) {
                return MapBounds.getRhyBounds($rootScope.account.rhyMembership.officialCode);
            }
            return null;
        };

        this.getPropertyPolygonByCode = function (propertyIdentifier) {
            var params = {propertyIdentifier: propertyIdentifier};

            return $http.get('/api/v1/gis/property/identifier', {params: params});
        };

        this.getPropertyPolygonById = function (id) {
            var params = {id: id};
            return $http.get('/api/v1/gis/property/id', {params: params});
        };

        this.getPropertyByCoordinates = function (latlng) {
            var params = angular.copy(latlng);

            return $http.get('/api/v1/gis/property/point', {params: params});
        };

        this.getPropertyByBounds = function (bounds) {
            var sw = bounds.getSouthWest();
            var ne = bounds.getNorthEast();

            var params = {
                minLat: sw.lat,
                minLng: sw.lng,
                maxLat: ne.lat,
                maxLng: ne.lng
            };

            return $http.get('/api/v1/gis/property/bounds', {params: params});
        };

        this.getMetsahallitusHirviById = function (id) {
            var params = {id: id};
            return $http.get('/api/v1/gis/mh/hirvi/id', {params: params});
        };

        this.listMetsahallitusHirviByYear = function (year) {
            var params = {year: year};
            return $http.get('/api/v1/gis/mh/hirvi', {params: params});
        };
    })

    .service('MapPdfModal', function ($uibModal, FormPostService) {
        this.printArea = function (url) {
            return $uibModal.open({
                controller: ModalController,
                templateUrl: 'common/map/map-pdf.html',
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    url: _.constant(url)
                }
            });
        };

        function ModalController($uibModalInstance, url) {
            var $ctrl = this;

            $ctrl.request = {
                paperSize: 'A4',
                paperDpi: '300',
                paperOrientation: 'PORTRAIT'
            };

            $ctrl.save = function () {
                $uibModalInstance.close();
                FormPostService.submitFormUsingBlankTarget(url, $ctrl.request);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss();
            };
        }
    })

    .service('Markers', function (WGS84, MapBounds) {
        // Transforms Javascript objects to Leaflet marker data.
        //
        // extractLeafletMarkerData function is expected to return object having
        // following structure:
        //
        // {
        //   id: <object id>
        //   etrsCoordinates: {
        //     latitude: <latitude in ETRS-TM35FIN coordinate system>
        //     longitude: <longitude in ETRS-TM35FIN coordinate system>
        //   },
        //   popupMessageFields: <either an array of objects or a function returning an array of objects (id passed as argument)>
        //   popupMessageButtons: <either an array of objects or a function returning an array of objects (id passed as argument)>
        //   getMessageScope: <function returning scope within which angular expressions are evaluated; defaults to $rootScope>
        // }
        //
        // popupMessageFields array must have the following form:
        // [
        //   { name: <message field name>, value: <message field value> },
        //   ...
        // ]
        //
        // popupMessageButtons array must have the following form:
        // [
        //   { name: <button name>, handlerExpr: <angular expression> },
        //   ...
        // ]
        //
        this.transformToLeafletMarkerData = function (objects, markerDefaults, extractLeafletMarkerData) {
            return _.flatten(_.map(objects, function (obj) {
                return _.map(extractLeafletMarkerData(obj), function (markerData) {
                    var id = markerData.id,
                        etrsLoc = markerData.etrsCoordinates,
                        wgs84Loc = WGS84.fromETRS(etrsLoc.latitude, etrsLoc.longitude),
                        messageFields = markerData.popupMessageFields,
                        messageButtons = markerData.popupMessageButtons,
                        clickHandler = markerData.clickHandler;

                    markerData = _.merge(angular.copy(markerDefaults), markerData, wgs84Loc);

                    if (markerData.message || messageFields || messageButtons) {
                        if (!markerData.message) {
                            markerData.message = function () {
                                if (angular.isFunction(messageFields)) {
                                    messageFields = messageFields(id);
                                }
                                if (angular.isFunction(messageButtons)) {
                                    messageButtons = messageButtons(id);
                                }
                                return formatMarkerPopupContent(messageFields, messageButtons);
                            };
                        }
                    } else if (clickHandler) {
                        // Noop (currently)
                    }
                    return markerData;

                });
            }));
        };

        function formatMarkerPopupContent(messageFields, buttons) {
            var popupContent = _.reduce(messageFields, function (accumulator, field) {
                var fieldName = field.name,
                    value = field.value,
                    localizationKey = fieldName ? 'global.marker_messages.' + fieldName : undefined,
                    hearderLine = fieldName ? '  <dt><span translate="' + localizationKey + '"></span>:</dt>\n' : '<dt/>\n',
                    valueLine = '  <dd>' + value + '</dd>\n';

                return accumulator + hearderLine + valueLine;
            }, '<dl class="r-leaflet-marker-popup-field">\n') + '</dl>\n';

            if (buttons && buttons.length > 0) {
                popupContent += _.reduce(buttons, function (accumulator, button) {
                    var localizationKey = 'global.marker_messages.' + button.name,
                        linkHtml = '<a ng-click="' + button.handlerExpr + '" class="btn btn-default">' +
                            '<span translate="' + localizationKey + '"></span>' +
                            '</a>\n';

                    return accumulator + linkHtml;
                }, '<div class="text-right">\n') + '</div>\n';
            }

            return popupContent;
        }

        this.getColorForHarvestReportState = function (state) {
            if (state === 'REJECTED') {
                return 'red';
            } else if (state === 'APPROVED' || state === 'ACCEPTED') {
                return 'green';
            } else if (state === 'SENT_FOR_APPROVAL') {
                return 'orange';
            } else if (state === 'PROPOSED') {
                return 'orange';
            }
            return 'red';
        };

        // markerClassFn is a string-returning function that takes
        // a L.MarkerClusterGroup object as a parameter.
        this.iconCreateFunction = function (markerClassFn) {
            return function (cluster) {
                return new L.DivIcon({
                    html: '<div><span>' + cluster.getChildCount() + '</span></div>',
                    className: 'marker-cluster marker-cluster-' + markerClassFn(cluster),
                    iconSize: new L.Point(40, 40)
                });
            };
        };

        this.getMarkerBounds = function (markers, defaultBounds) {
            var markerToLatLng = function (marker) {
                return {
                    lat: marker.lat,
                    lng: marker.lng
                };
            };

            return MapBounds.getBounds(markers, markerToLatLng, defaultBounds);
        };
    });