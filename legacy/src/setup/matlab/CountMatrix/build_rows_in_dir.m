function rows = build_rows_in_dir (path,a,b,bin_size)

files = dir (sprintf ('%s/*.spk',path));
num_files = size (files,1);
rows=[];
for i=1:num_files,
    cmd = sprintf ('load %s/%s',path,files(i).name);
    eval(cmd);
    cmd = sprintf ('spikes=%s;row=build_row (a,b,bin_size,spikes); ',strrep(files(i).name,'.spk',''));
    eval (cmd);
    %rows = [rows; row];
    disp (sprintf('{%s},',row));
end
return;
