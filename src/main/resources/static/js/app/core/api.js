(function (window, $) {
    var FarmApi = {};

    function callIfFunction(callback) {
        if ($.isFunction(callback)) {
            callback.apply(null, Array.prototype.slice.call(arguments, 1));
        }
    }

    function request(options, onSuccess, onError) {
        var opts = $.extend({
            url: "",
            type: "get",
            dataType: "json"
        }, options || {});

        $.ajax($.extend({}, opts, {
            success: function (res) {
                callIfFunction(onSuccess, res);
            },
            error: function (xhr, status) {
                callIfFunction(onError, xhr, status);
            }
        }));
    }

    function get(url, onSuccess, onError) {
        request({
            url: url,
            type: "get"
        }, onSuccess, onError);
    }

    function post(url, params, onSuccess, onError) {
        request({
            url: url,
            type: "post",
            contentType: "application/json",
            data: JSON.stringify(params || {})
        }, onSuccess, onError);
    }

    FarmApi.isOk = function (res) {
        var code = Number(res && res.code);
        return code === 0 || code === 200;
    };

    FarmApi.request = request;
    FarmApi.get = get;
    FarmApi.post = post;

    FarmApi.getCurUser = function (onSuccess, onError) {
        get("/user/getCurUser", onSuccess, onError);
    };

    FarmApi.getCurUserSettings = function (onSuccess, onError) {
        get("/user/settings/get", onSuccess, onError);
    };

    FarmApi.saveCurUserSettings = function (params, onSuccess, onError) {
        post("/user/settings/save", params, onSuccess, onError);
    };

    FarmApi.loginOptions = function (onSuccess, onError) {
        get("/user/loginOptions", onSuccess, onError);
    };

    FarmApi.setCurUser = function (userId, onSuccess, onError) {
        post("/user/setCurUser", {id: Number(userId || 0)}, onSuccess, onError);
    };

    FarmApi.userAdminPage = function (params, onSuccess, onError) {
        post("/user/gridDataFilterSortPage", params, onSuccess, onError);
    };

    FarmApi.userAdminSave = function (params, onSuccess, onError) {
        post("/user/addOrUpdate", params, onSuccess, onError);
    };

    FarmApi.userAdminDelete = function (params, onSuccess, onError) {
        post("/user/delete", params, onSuccess, onError);
    };

    FarmApi.myFarmOverview = function (userId, onSuccess, onError) {
        post("/gameplay/myFarmOverview", {userId: Number(userId || 0)}, onSuccess, onError);
    };

    FarmApi.myPlantingPanel = function (userId, onSuccess, onError) {
        post("/gameplay/myPlantingPanel", {userId: Number(userId || 0)}, onSuccess, onError);
    };

    FarmApi.seedPlantablePlots = function (userId, seedTypeId, onSuccess, onError) {
        post("/gameplay/seedPlantablePlots", {
            userId: Number(userId || 0),
            seedTypeId: Number(seedTypeId || 0)
        }, onSuccess, onError);
    };

    FarmApi.plant = function (params, onSuccess, onError) {
        post("/gameplay/plant", params, onSuccess, onError);
    };

    FarmApi.harvest = function (params, onSuccess, onError) {
        post("/gameplay/harvest", params, onSuccess, onError);
    };

    FarmApi.clear = function (params, onSuccess, onError) {
        post("/gameplay/clear", params, onSuccess, onError);
    };

    FarmApi.care = function (params, onSuccess, onError) {
        post("/gameplay/care", params, onSuccess, onError);
    };

    FarmApi.plotUnlock = function (params, onSuccess, onError) {
        post("/gameplay/plot/unlock", params, onSuccess, onError);
    };

    FarmApi.plotExpand = function (params, onSuccess, onError) {
        post("/gameplay/plot/expand", params, onSuccess, onError);
    };

    FarmApi.plotExpandOptions = function (params, onSuccess, onError) {
        post("/gameplay/plot/expand/options", params, onSuccess, onError);
    };

    FarmApi.listSoilOptions = function (onSuccess, onError) {
        get("/seed/soil/options", onSuccess, onError);
    };

    FarmApi.plotSoilPage = function (params, onSuccess, onError) {
        post("/plot/soil/page", params, onSuccess, onError);
    };

    FarmApi.plotSoilSave = function (params, onSuccess, onError) {
        post("/plot/soil/save", params, onSuccess, onError);
    };

    FarmApi.plotSoilGet = function (params, onSuccess, onError) {
        post("/plot/soil/get", params, onSuccess, onError);
    };

    FarmApi.plotSoilDelete = function (params, onSuccess, onError) {
        post("/plot/soil/delete", params, onSuccess, onError);
    };

    FarmApi.plotPolicyCurrent = function (params, onSuccess, onError) {
        post("/plot/policy/current", params, onSuccess, onError);
    };

    FarmApi.plotPolicySave = function (params, onSuccess, onError) {
        post("/plot/policy/save", params, onSuccess, onError);
    };

    FarmApi.plotPolicyActivate = function (params, onSuccess, onError) {
        post("/plot/policy/activate", params, onSuccess, onError);
    };

    FarmApi.listSeedQualityOptions = function (onSuccess, onError) {
        get("/seed/quality/options", onSuccess, onError);
    };

    FarmApi.shopHome = function (params, onSuccess, onError) {
        post("/seed/shop/home", params, onSuccess, onError);
    };

    FarmApi.shopPage = function (params, onSuccess, onError) {
        post("/seed/shop/page", params, onSuccess, onError);
    };

    FarmApi.shopBuy = function (params, onSuccess, onError) {
        post("/seed/shop/buy", params, onSuccess, onError);
    };

    FarmApi.fruitPage = function (params, onSuccess, onError) {
        post("/seed/shop/fruit/page", params, onSuccess, onError);
    };

    FarmApi.seedInventoryPage = function (params, onSuccess, onError) {
        post("/seed/shop/seed/page", params, onSuccess, onError);
    };

    FarmApi.sellFruit = function (params, onSuccess, onError) {
        post("/seed/shop/sell-fruit", params, onSuccess, onError);
    };

    FarmApi.shopTradePage = function (params, onSuccess, onError) {
        post("/seed/shop/trade/page", params, onSuccess, onError);
    };

    FarmApi.listGrowthStageOptions = function (onSuccess, onError) {
        get("/seed/growth-stage/options", onSuccess, onError);
    };

    FarmApi.seedTypePage = function (params, onSuccess, onError) {
        post("/seed/type/page", params, onSuccess, onError);
    };

    FarmApi.seedTypeSave = function (params, onSuccess, onError) {
        post("/seed/type/save", params, onSuccess, onError);
    };

    FarmApi.seedTypeDelete = function (params, onSuccess, onError) {
        post("/seed/type/delete", params, onSuccess, onError);
    };

    FarmApi.seedStagePage = function (params, onSuccess, onError) {
        post("/seed/stage/page", params, onSuccess, onError);
    };

    FarmApi.seedStageSave = function (params, onSuccess, onError) {
        post("/seed/stage/save", params, onSuccess, onError);
    };

    FarmApi.seedStageDelete = function (params, onSuccess, onError) {
        post("/seed/stage/delete", params, onSuccess, onError);
    };

    window.FarmApi = FarmApi;
})(window, window.jQuery);
