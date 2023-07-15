package com.clinic.controller;

import com.clinic.entity.SignEntity;
import com.clinic.entity.StaffEntity;
import com.clinic.params.SignParams;
import com.clinic.service.StaffService;
import com.clinic.util.DateUtil;
import com.clinic.util.PhoneValidationUtil;
import com.clinic.util.TimesheetUtil;
import com.clinic.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Tag(name = "员工", description = "员工API")
@RestController
@Slf4j
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }


    @Operation(summary = "创建新员工", description = "创建一个新员工",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StaffView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "员工表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StaffView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionView.class)
                    ))
            })
    @PostMapping("/")
    public StaffView createStaff(@Validated @RequestBody StaffView staffView) {
        String account = staffView.getAccount();
        String password = staffView.getPassword();
        if (password == null || password.length() < 2 || password.length() > 20){
            throw new IllegalArgumentException("密码长度范围2-20");
        }
        staffService.findByAccount(account).ifPresent(staffEntity -> {
            throw new IllegalArgumentException("员工已存在");
        });
        String phone = staffView.getPhone();
        if (phone != null && !phone.isEmpty()){
            phone = phone.trim();
            if (phone.startsWith("+86")){
                phone = phone.substring(3);
            }
            if (!PhoneValidationUtil.isPhone(phone)){
                throw new IllegalArgumentException("请输入正确的手机号码");
            }
            staffService.findByPhone(phone).ifPresent(staffEntity -> {
                throw new IllegalArgumentException("该手机号码已经绑定了员工");
            });
        }
        StaffEntity staffEntity = staffService.create(account, password);
        staffEntity.setDefaultValue();
        if (!staffView.getName().isEmpty()){
            staffEntity.setName(staffView.getName());
        }
        if (phone != null && !phone.isEmpty()){
            staffEntity.setPhone(phone);
        }
        Integer role = staffView.getRole();
        if (role != null){
            staffEntity.setRole(role);
        }
        String address = staffView.getAddress();
        if (address!= null && !address.isEmpty()){
            staffEntity.setAddress(address);
        }
        Integer age = staffView.getAge();
        if (age != null){
            staffEntity.setAge(age);
        }
        Date birthday = staffView.getBirthday();
        if ( birthday!= null) {
            staffEntity.setBirthday(birthday);
        }else{
            staffEntity.setBirthday(new Date());
        }
        StaffEntity save = staffService.save(staffEntity);
        return StaffView.fromStaffEntity(save);
    }

    @Operation(summary = "更新员工信息", description = "更新员工信息",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StaffView.class)
            ), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "员工表", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StaffView.class)
                    )),
                    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageView.class)
                    )),
                    @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionView.class)
                    ))
            })
    @PutMapping("/")
    public StaffView updateStaffById(@Validated @RequestBody StaffView staffView) {
        StaffEntity staffEntity = staffService.findById(staffView.getId()).orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        staffView.updateStaffEntity(staffEntity);
        StaffEntity save = staffService.save(staffEntity);
        String password = staffView.getPassword();
        if (password != null ){
            save = staffService.updatePassword(staffEntity, password);
        }
        return StaffView.fromStaffEntity(save);
    }

    @Operation(summary = "根据ID查找员工", description = "根据员工的id来查找员工", responses = {
            @ApiResponse(responseCode = "200", description = "员工表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StaffView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @GetMapping("/{id}")
    public StaffView findStaffById(@PathVariable @NotNull Long id) {
        StaffEntity staffEntity = staffService.findById(id).orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        return StaffView.fromStaffEntity(staffEntity);
    }

    @GetMapping("/page")
    public PageResp<StaffView> findByPage(@Validated @NotNull @RequestParam int current, @Validated @NotNull @RequestParam int pageSize, @Validated @Nullable @RequestParam String name, @Validated @Nullable @RequestParam Integer age, @Validated @Nullable @RequestParam String phone, @Validated @Nullable @RequestParam String startTime, @Validated @Nullable @RequestParam String endTime){
        PageRequest page = PageRequest.of(current>0?current-1:0, pageSize>0?pageSize:1);
        Page<StaffEntity> allByPage = staffService.findAllByPage(name,age,phone,startTime,endTime,page);
        List<StaffView> data = new ArrayList<>();
        for (StaffEntity staff : allByPage) {
            data.add(StaffView.fromStaffEntity(staff));
        }
        PageResp<StaffView> staffViewPageResp = new PageResp<StaffView>();
        staffViewPageResp.data = data;
        staffViewPageResp.pages = allByPage.getTotalPages();
        staffViewPageResp.total = allByPage.getTotalElements();
        staffViewPageResp.success = true;
        return staffViewPageResp;
    }

    @Operation(summary = "根据名字查找员工", description = "根据员工的名字来查找员工", responses = {
            @ApiResponse(responseCode = "200", description = "员工表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StaffView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @GetMapping("/name/{name}")
    public StaffView findStaffByName(String name) {
        StaffEntity staffEntity = staffService.findByName(name).orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        return StaffView.fromStaffEntity(staffEntity);
    }

    @Operation(summary = "根据手机号码查找员工", description = "根据员工的手机号码来查找员工", responses = {
            @ApiResponse(responseCode = "200", description = "员工表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StaffView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @GetMapping("/phone/{phone}")
    public StaffView findStaffByPhone(String phone) {
        StaffEntity staffEntity = staffService.findByName(phone).orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        return StaffView.fromStaffEntity(staffEntity);
    }

    @Operation(summary = "员工请假", description = "员工请假", responses = {
            @ApiResponse(responseCode = "200", description = "员工表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StaffView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @PostMapping("/leave")
    public StaffView staffAskLeave() {
        return StaffView.builder().build();
    }

    @Operation(summary = "删除员工", description = "根据员工的id来删除员工", responses = {
            @ApiResponse(responseCode = "200", description = "员工表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StaffView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @DeleteMapping("/{id}")
    public StaffView deleteStaffById(@PathVariable("id") Long id) {
        StaffEntity staffEntity = staffService.findById(id).orElseThrow(() -> new IllegalArgumentException("员工不存在"));
        staffService.deleteById(id);
        return StaffView.fromStaffEntity(staffEntity);
    }

    @Operation(summary = "员工签到", description = "员工签到", responses = {
            @ApiResponse(responseCode = "200", description = "签到表", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SignView.class)
            )),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "401", description = "没有权限", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageView.class)
            )),
            @ApiResponse(responseCode = "500", description = "服务器参数", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionView.class)
            ))
    })
    @PostMapping("/sign")
    public SignView sign(HttpServletRequest request,@Validated @RequestBody SignParams signParams ) throws Exception {
        StaffEntity currentStaff = (StaffEntity)request.getSession().getAttribute("currentStaff");
        SignEntity signResult = staffService.sign(currentStaff.getId(), signParams.getSignType());
        return SignView.FromSignEntity(signResult);
    }

    @GetMapping("/timesheet/today")
    public TimesheetDayView todayTimesheet(HttpServletRequest request) throws Exception {
        StaffEntity currentStaff = (StaffEntity)request.getSession().getAttribute("currentStaff");
        List<SignEntity> todayTimesheetList = staffService.getTodayTimesheetList(currentStaff.getId());
        TimesheetDayView.TimesheetDayViewBuilder builder = TimesheetDayView.builder();
        ArrayList<TimesheetView> timesheetViews = new ArrayList<>();
        for (SignEntity signEntity : todayTimesheetList) {
            TimesheetView.TimesheetViewBuilder tb = TimesheetView.builder();
            tb.name(currentStaff.getName());
            tb.signType(signEntity.getType());
            tb.time(DateUtil.stdFormat(signEntity.getCreateTime()));
            timesheetViews.add(tb.build());
        }
        TimesheetUtil.DayFilter dayFilter = TimesheetUtil.DayFilter.of(todayTimesheetList);
        dayFilter.statistic(new Date());
        Date start = dayFilter.getStart();
        Date end = dayFilter.getEnd();
        builder.data(timesheetViews);
        if ( start!=null ){
            builder.startTime(DateUtil.stdFormat(start));
        }
        if (end != null ){
            builder.endTime(DateUtil.stdFormat(end));
        }
        builder.total(dayFilter.getHours());
        return builder.build();
    }
    @GetMapping("/timesheet/month")
    public TimesheetMouthView mouthTimesheet(@NotNull @RequestParam Long month) throws Exception {
        DateUtil.DateRange dateRange = DateUtil.DateRange.ofMonth(month.intValue());
        if (dateRange == null ){
            throw new IllegalArgumentException("无效的月份");
        }
        PageRequest page = PageRequest.ofSize(Integer.MAX_VALUE);
        Page<SignEntity> signAllByPage = staffService.findSignAllByPage(null, DateUtil.stdFormat(dateRange.getStart()), DateUtil.stdFormat(dateRange.getEnd()), page);
        List<SignEntity> list = signAllByPage.toList();
        TimesheetUtil.TimesheetFilter timesheetFilter = TimesheetUtil.TimesheetFilter.of(list);
        timesheetFilter.statistic(new Date());
        Map<Long, TimesheetUtil.StaffFilter> data = timesheetFilter.getData();
        TimesheetMouthView.TimesheetMouthViewBuilder builder = TimesheetMouthView.builder();
        builder.mouth(DateUtil.getMonth());
        List<TimesheetStaffView> timesheetStaffViews = new ArrayList<>();
        for (Map.Entry<Long, TimesheetUtil.StaffFilter> longStaffFilterEntry : data.entrySet()) {
            TimesheetStaffView.TimesheetStaffViewBuilder staffViewBuilder = TimesheetStaffView.builder();
            Long staffId = longStaffFilterEntry.getKey();
            Optional<StaffEntity> staffEntityOptional = staffService.findById(staffId);
            if (staffEntityOptional.isEmpty()){
                continue;
            }
            StaffEntity staffEntity = staffEntityOptional.get();
            String name = staffEntity.getName();
            staffViewBuilder.staffId(staffId);
            staffViewBuilder.name(name);
            Collection<TimesheetUtil.DayFilter> values = longStaffFilterEntry.getValue().getData().values();
            List<TimesheetDayView> dayViews = new ArrayList<>();
            for (TimesheetUtil.DayFilter value : values) {
                TimesheetDayView.TimesheetDayViewBuilder dayBuilder = TimesheetDayView.builder();
                dayBuilder.endTime(value.getShortEnd());
                dayBuilder.startTime(value.getShortStart());
                dayBuilder.total(value.getHours());
                dayBuilder.day(value.getDay());
                dayViews.add(dayBuilder.build());
            }
            staffViewBuilder.data(dayViews);
            staffViewBuilder.total(longStaffFilterEntry.getValue().getHours());
            timesheetStaffViews.add(staffViewBuilder.build());
        }
        builder.data(timesheetStaffViews);
        return builder.build();
    }
}
