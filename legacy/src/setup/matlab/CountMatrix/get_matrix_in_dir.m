function M = get_matrix_in_dir (path,a,b,bin_size)

files = dir (sprintf ('%s/*.spk',path));
num_files = size (files,1);
M=[];
for i=1:num_files,
    cmd = sprintf ('load %s/%s',path,files(i).name);
    eval(cmd);
    cmd = sprintf ('spikes=%s;row=get_row (a,b,bin_size,spikes); ',strrep(files(i).name,'.spk',''));
    eval (cmd);
    M = [M; row'];
    
end
return;