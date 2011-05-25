function java_code = build_row (a,b,bin_size,spikes)

x=a:bin_size:b;
y=histc(spikes,x);
values = strtrim(num2str(y','%02d '));
values = strrep(values,' ',',');
java_code = values;
return;
