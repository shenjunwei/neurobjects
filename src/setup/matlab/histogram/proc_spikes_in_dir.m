function proc_spikes_in_dir (path,a,b,bin_size)

files = dir (sprintf ('%s/*.spk',path));
num_files = size (files,1);
for i=1:num_files,
    proc_spikes (path,files(i).name,bin_size,a,b);
end
code=[];
for i=1:num_files,
    code = sprintf ('%s %s',code,strrep(files(i).name,'.spk',''));
end

code = strrep(strtrim (code),' ',',');
disp (sprintf ('double spikes[][] = {%s};',code));
code=[];
for i=1:num_files,
    code = sprintf ('%s %s_h',code,strrep(files(i).name,'.spk',''));
end

code = strrep(strtrim (code),' ',',');
disp (sprintf ('int spikes_h[][] = {%s};',code));
return;