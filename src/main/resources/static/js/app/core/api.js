(function (window, $) {
    var FarmApi = {};
    FarmApi.isOk = function (res) {
        var code = Number(res && res.code);
        return code === 0 || code === 200;
    };

    FarmApi.getCurUser = function (onSuccess, onError) {
        $.ajax({
            url: "/user/getCurUser",
            type: "get",
            dataType: "json",
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.getCurUserSettings = function (onSuccess, onError) {
        $.ajax({
            url: "/user/settings/get",
            type: "get",
            dataType: "json",
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.saveCurUserSettings = function (params, onSuccess, onError) {
        $.ajax({
            url: "/user/settings/save",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.loginOptions = function (onSuccess, onError) {
        $.ajax({
            url: "/user/loginOptions",
            type: "get",
            dataType: "json",
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.setCurUser = function (userId, onSuccess, onError) {
        $.ajax({
            url: "/user/setCurUser",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({id: Number(userId || 0)}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.userAdminPage = function (params, onSuccess, onError) {
        $.ajax({
            url: "/user/gridDataFilterSortPage",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.userAdminSave = function (params, onSuccess, onError) {
        $.ajax({
            url: "/user/addOrUpdate",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.userAdminDelete = function (params, onSuccess, onError) {
        $.ajax({
            url: "/user/delete",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.myFarmOverview = function (userId, onSuccess, onError) {
        $.ajax({
            url: "/gameplay/myFarmOverview",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({userId: Number(userId || 0)}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.myPlantingPanel = function (userId, onSuccess, onError) {
        $.ajax({
            url: "/gameplay/myPlantingPanel",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({userId: Number(userId || 0)}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.seedPlantablePlots = function (userId, seedTypeId, onSuccess, onError) {
        $.ajax({
            url: "/gameplay/seedPlantablePlots",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({userId: Number(userId || 0), seedTypeId: Number(seedTypeId || 0)}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.plant = function (params, onSuccess, onError) {
        $.ajax({
            url: "/gameplay/plant",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.harvest = function (params, onSuccess, onError) {
        $.ajax({
            url: "/gameplay/harvest",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.care = function (params, onSuccess, onError) {
        $.ajax({
            url: "/gameplay/care",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.plotUnlock = function (params, onSuccess, onError) {
        $.ajax({
            url: "/gameplay/plot/unlock",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.plotExpand = function (params, onSuccess, onError) {
        $.ajax({
            url: "/gameplay/plot/expand",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.listSoilOptions = function (onSuccess, onError) {
        $.ajax({
            url: "/seed/soil/options",
            type: "get",
            dataType: "json",
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.listSeedQualityOptions = function (onSuccess, onError) {
        $.ajax({
            url: "/seed/quality/options",
            type: "get",
            dataType: "json",
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.shopHome = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/shop/home",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.shopPage = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/shop/page",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.shopBuy = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/shop/buy",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.fruitPage = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/shop/fruit/page",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.seedInventoryPage = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/shop/seed/page",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.sellFruit = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/shop/sell-fruit",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.shopTradePage = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/shop/trade/page",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.listGrowthStageOptions = function (onSuccess, onError) {
        $.ajax({
            url: "/seed/growth-stage/options",
            type: "get",
            dataType: "json",
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.seedTypePage = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/type/page",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.seedTypeSave = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/type/save",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.seedTypeDelete = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/type/delete",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.seedStagePage = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/stage/page",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.seedStageSave = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/stage/save",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    FarmApi.seedStageDelete = function (params, onSuccess, onError) {
        $.ajax({
            url: "/seed/stage/delete",
            type: "post",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(params || {}),
            success: function (res) {
                if ($.isFunction(onSuccess)) {
                    onSuccess(res);
                }
            },
            error: function (xhr, status) {
                if ($.isFunction(onError)) {
                    onError(xhr, status);
                }
            }
        });
    };

    window.FarmApi = FarmApi;
})(window, window.jQuery);
