function java_code = build_java_code (a,b,bin_size,spikes,spikes_name)

x=a:bin_size:b;
y=histc(spikes,x);
values = strtrim(num2str(spikes','%2.4f '));
values = strrep(values,' ',',');
code = sprintf ('double %s[]={%s};',spikes_name,values);
values = strtrim(num2str(y','%d '));
values = strrep(values,' ',',');
code = sprintf ('%s\ndouble %s_h[]={%s};',code,spikes_name,values);
java_code = code;
return;
