<!DOCTYPE html>
<html lang="en">
<head>
    <#include "./inc/resource.ftl">
    <title>主机管理</title>
</head>
<body>

<#include "./inc/header.ftl">
<div class="container-fluid content">
    <div class="row">
        <#include "./inc/siderbar.ftl">
        <div class="main">
            <div class="row">
                <div class="col-lg-12">
                    <h3 class="page-header"><i class="fa fa-laptop"></i> Dashboard</h3>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            主机列表
                            <button class="btn btn-primary pull-right" id="btn-add"><i class="fa fa-plus"></i> 添加主机</button>
                        </div>
                        <div class="panel-body">
                            <div id="host-table"></div>
                            <ul class="pagination"></ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<#include "./inc/footer.ftl">
<script type="text/html" id="form-add">
    <form method="post" class="form-horizontal">
        <div class="form-group">
            <label class="col-md-3 control-label" for="text-input">主机名称：</label>
            <div class="col-md-9">
                <input type="text" id="name" name="name" class="form-control" placeholder="比如公司电脑">
            </div>
        </div>
        <br>
    </form>
</script>
<script type="text/javascript">
    $(document).ready(function()
    {
        $('#btn-add').click(function()
        {
            modal({
                title : '添加新主机',
                html : $('#form-add').html(),
                close : true,
                ok : function(dialog)
                {
                    var name = $.trim(dialog.find('#name').val());
                    if (name.length == 0) return alert('请填写主机名称'), false;
                    $.post('${context}/host/add', { name : name }, function(result)
                    {
                        if (result.error.code != 0) return alert(result.error.reason);
                        $('#host-table').paginate('reload');
                    });
                }
            });
        });

        $('#host-table').paginate({
            url : '${context}/host/json',
            paginate : $('.pagination'),
            fields : [
                {
                    title : '#',
                    name : 'id',
                    width : '100',
                    align : 'center',
                    formatter : function(i, v, r)
                    {
                        return i + 1;
                    }
                },
                {
                    title : '名称',
                    name : 'name',
                },
                {
                    title : 'IP',
                    name : 'ip',
                    width : '140',
                    formatter : function(i, v, r)
                    {
                        return v == null || typeof(v) == 'undefined' ? '--' : v;
                    }
                },
                {
                    title : '访问令牌',
                    width : '400',
                    align : 'center',
                    name : 'accesstoken',
                },
                {
                    title : '状态',
                    width : '80',
                    align : 'center',
                    name : 'state',
                    formatter : function(i, v, r)
                    {
                        if (v == 1) return '离线';
                        if (v == 2) return '在线';
                    }
                },
                {
                    title : '最近通信时间',
                    width : '160',
                    align : 'center',
                    name : 'lastActiveTime',
                    formatter : function(i, v, r)
                    {
                        if (v > 0) return new Date(v).format('yyyy-MM-dd hh:mm:ss');
                        else return '--';
                    }
                },
                {
                    title : '操作',
                    name : 'id',
                    align : 'center',
                    width : '200',
                    formatter : function(i, v, r)
                    {
                        return '<a href="javascript:;" id="btn-edit" x-host-id="' + v + '" class="btn btn-primary"><i class="fa fa-edit"></i> 修改</a>' +
                                '<a href="javascript:;" id="btn-renew" x-host-id="' + v + '" class="btn btn-danger"><i class="fa fa-key"></i> 更新令牌</a>';
                    }
                }
            ]
        });

        $(document).on('click', 'a[id=btn-renew]', function()
        {
            var id = $(this).attr('x-host-id');
            modal({
                title : '',
                text : '真的要重置此主机的令牌吗？',
                close : true,
                ok : function()
                {
                    $.post('${context}/host/renew', { id : id }, function(result)
                    {
                        if (result.error.code != 0) return alert(result.error.reason);
                        $('#host-table').paginate('reload');
                    });
                }
            });
        });

    });
</script>
</body>
</html>